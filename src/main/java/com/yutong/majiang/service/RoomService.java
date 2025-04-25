package com.yutong.majiang.service;

import com.yutong.majiang.constant.Constants;
import com.yutong.majiang.dto.RoomPlayerDTO;
import com.yutong.majiang.dto.RoomRecordDTO;
import com.yutong.majiang.entity.*;
import com.yutong.majiang.repository.GameRoomPlayerRepository;
import com.yutong.majiang.repository.GameRoomRecordRepository;
import com.yutong.majiang.repository.GameRoomRepository;
import com.yutong.majiang.request.FlyZhongInfo;
import com.yutong.majiang.request.GangInfo;
import com.yutong.majiang.request.HuDetail;
import com.yutong.majiang.response.Player;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class RoomService {

    private GameRoomRepository gameRoomRepository;

    private GameRoomPlayerRepository gameRoomPlayerRepository;

    private UserService userService;

    private GameRoomRecordRepository gameRoomRecordRepository;

    public String createRoom(String creator) {
        GameRoom gameRoom = new GameRoom();
        String roomId = RandomStringUtils.randomAlphanumeric(6).toUpperCase();
        while(gameRoomRepository.findById(roomId).isPresent()) {
            //If the current roomId is exist,
            roomId = RandomStringUtils.randomAlphanumeric(6).toUpperCase();
        }
        gameRoom.setRoomId(roomId);
        gameRoom.setCreator(creator);
        gameRoom.setStatus(Constants.ROOM_STATUS_IN_PROGRESS);
        gameRoom.setStartTime(new Timestamp(System.currentTimeMillis()));

        gameRoomRepository.saveAndFlush(gameRoom);
        log.info("Create room {} successfully", roomId);
        return roomId;
    }

    public void addUser(String roomId, String userId) {
        GameRoomPlayerID gameRoomPlayerID = new GameRoomPlayerID();
        gameRoomPlayerID.setRoomId(roomId);
        gameRoomPlayerID.setUserId(userId);
        if(gameRoomPlayerRepository.findById(gameRoomPlayerID).isEmpty()){
            GameRoomPlayer gameRoomPlayer = new GameRoomPlayer();
            gameRoomPlayer.setRoomId(roomId);
            gameRoomPlayer.setUserId(userId);
            GameRoom gameRoom = gameRoomRepository.findById(roomId).orElse(null);
            if(gameRoom != null){
                gameRoomPlayer.setIsHost(gameRoom.getCreator().equals(userId) ? 1 : 0);
            }else{
                gameRoomPlayer.setIsHost(0);
            }
            gameRoomPlayer.setPoints(0);
            gameRoomPlayer.setJoinedDate(new Timestamp(System.currentTimeMillis()));
            gameRoomPlayerRepository.saveAndFlush(gameRoomPlayer);

            log.info("Add user {} to room {} successfully", userId, roomId);
        }
    }

    public void removeUser(String roomId, String userId) {
        GameRoomPlayerID gameRoomPlayerID = new GameRoomPlayerID();
        gameRoomPlayerID.setRoomId(roomId);
        gameRoomPlayerID.setUserId(userId);
        gameRoomPlayerRepository.deleteById(gameRoomPlayerID);
        log.info("Remove user {} from room {} successfully", userId, roomId);
    }

    public List<RoomPlayerDTO> getUsersByRoomId(String roomId) {
        List<GameRoomPlayer> gameRoomPlayerList = gameRoomPlayerRepository.findGameRoomPlayersByRoomId(roomId);
        List<RoomPlayerDTO> roomPlayerDTOList = new ArrayList<>();
        for(GameRoomPlayer gameRoomPlayer : gameRoomPlayerList) {
            MajiangUser majiangUser = userService.getUserDetailById(gameRoomPlayer.getUserId());
            RoomPlayerDTO roomPlayerDTO = new RoomPlayerDTO();
            roomPlayerDTO.setMajiangUser(majiangUser);
            roomPlayerDTO.setIsHost(1 == gameRoomPlayer.getIsHost());
            roomPlayerDTO.setTotalPoints(gameRoomPlayer.getPoints());
            roomPlayerDTO.setCurPoints(0);
            roomPlayerDTOList.add(roomPlayerDTO);
        }
        return roomPlayerDTOList;
    }

    public boolean isRoomExist(String roomId) {
        return !CollectionUtils.isEmpty(gameRoomRepository.findByRoomIdAndStatus(roomId, 0));

    }

    public void closeRoom(String roomId) {
        GameRoom gameRoom = gameRoomRepository.findById(roomId).orElse(null);
        if(gameRoom != null){
            gameRoom.setStatus(1);
            gameRoom.setEndTime(new Timestamp(System.currentTimeMillis()));
            gameRoomRepository.saveAndFlush(gameRoom);
            log.info("Close room {} successfully", roomId);
        }
    }

    public RoomRecordDTO calculate(HuDetail huDetail, String roomId) {
        log.info("Calculate hu details for room: {} start", roomId);
        List<RoomPlayerDTO> roomPlayerDTOList = this.getUsersByRoomId(roomId);
        int playerCount = roomPlayerDTOList.size();
        Integer basePoints = huDetail.getBasePoint();
        Integer actualPoints = basePoints;
        if(huDetail.getIsGangKai()) {
            actualPoints = basePoints * 2;
        }
        if(huDetail.getFourZhongWin()) {
            actualPoints = basePoints * 4;
        }else {
            actualPoints += huDetail.getMaCount() * actualPoints;
        }

        //处理基础胡牌信息
        for(RoomPlayerDTO roomPlayerDTO : roomPlayerDTOList) {
            if(roomPlayerDTO.getMajiangUser().getUserId().equals(huDetail.getHuPlayer().getUserId())) {
                roomPlayerDTO.setCurPoints(actualPoints * (playerCount - 1));
            }else {
                roomPlayerDTO.setCurPoints(-actualPoints);
            }
        }
        //处理飞红中信息
        if(!CollectionUtils.isEmpty(huDetail.getFlyZhongInfoList())) {
            log.info("Process fly zhong info");
            RoomPlayerDTO huPlayer = roomPlayerDTOList.stream().filter(p -> p.getMajiangUser().getUserId().equals(huDetail.getHuPlayer().getUserId())).findFirst().get();
            for(FlyZhongInfo flyZhongInfo : huDetail.getFlyZhongInfoList()) {
                int times = (int) Math.pow(2, flyZhongInfo.getZhongCount());
                //赢家飞红中
               if(flyZhongInfo.getZhongPlayer().getUserId().equals(huDetail.getHuPlayer().getUserId())) {
                    huPlayer.setCurPoints(huPlayer.getCurPoints() * times);
                    List<RoomPlayerDTO> losePlayers = roomPlayerDTOList.stream().filter(p -> !p.getMajiangUser().getUserId().equals(huPlayer.getMajiangUser().getUserId())).toList();
                    for(RoomPlayerDTO losePlayer : losePlayers) {
                       losePlayer.setCurPoints(losePlayer.getCurPoints() * times);
                    }
                }
               else {
                   //输家飞红中，输的分数翻倍
                   RoomPlayerDTO losePlayer = roomPlayerDTOList.stream().filter(p -> p.getMajiangUser().getUserId().equals(flyZhongInfo.getZhongPlayer().getUserId())).findFirst().get();
                   int losePoints = losePlayer.getCurPoints();
                   losePlayer.setCurPoints(losePoints * times);
                   //胡牌玩家获取输家翻倍的分数
                   huPlayer.setCurPoints(Math.abs(losePlayer.getCurPoints() - losePoints) + huPlayer.getCurPoints());
               }
            }
        }

        //处理杠信息
        if(!CollectionUtils.isEmpty(huDetail.getGangInfoList())) {
            log.info("Process Gang info");
            for(GangInfo gangInfo : huDetail.getGangInfoList()) {
                int winPoints = 0;
                int losePoints = 0;
               RoomPlayerDTO winPlayer = roomPlayerDTOList.stream().filter(p -> p.getMajiangUser().getUserId().equals(gangInfo.getWinPlayer().getUserId())).findFirst().get();
               //暗杠
               if(gangInfo.getLosePlayer().getUserId().equals("-1") //暗杠
                       || gangInfo.getLosePlayer().getUserId().equals("-2") ) { //臭杠
                   int baseCount = gangInfo.getLosePlayer().getUserId().equals("-1") ? 2 : 1;
                   List<RoomPlayerDTO> losePlayers = roomPlayerDTOList.stream().filter(p -> !p.getMajiangUser().getUserId().equals(gangInfo.getWinPlayer().getUserId())).toList();
                   winPoints = baseCount * (playerCount - 1) * basePoints;
                   losePoints = baseCount * basePoints;
                   winPlayer.setCurPoints(winPlayer.getCurPoints() + winPoints);
                   for(RoomPlayerDTO losePlayer : losePlayers) {
                       losePlayer.setCurPoints(losePlayer.getCurPoints() - losePoints);
                   }
               }else {
                   RoomPlayerDTO losePlayer = roomPlayerDTOList.stream().filter(p -> p.getMajiangUser().getUserId().equals(gangInfo.getLosePlayer().getUserId())).findFirst().get();
                   winPoints = 3 * basePoints;
                   losePoints = 3 * basePoints;
                   winPlayer.setCurPoints(winPlayer.getCurPoints() + winPoints);
                   losePlayer.setCurPoints(losePlayer.getCurPoints() - losePoints);
               }
            }
        }

        RoomRecordDTO roomRecordDTO = new RoomRecordDTO();
        roomRecordDTO.setPlayerRecordList(roomPlayerDTOList);
        roomRecordDTO.setCreateTime(new Timestamp(System.currentTimeMillis()));

        //Save points for current round
        for(RoomPlayerDTO roomPlayerDTO : roomPlayerDTOList) {
            GameRoomRecord gameRoomRecord = new GameRoomRecord();
            gameRoomRecord.setRoomId(roomId);
            gameRoomRecord.setUserId(roomPlayerDTO.getMajiangUser().getUserId());
            int maxSeqNo = Optional.ofNullable(gameRoomRecordRepository.getMaxSeqNoByRoomId(roomId)).orElse(-1);
            gameRoomRecord.setSeqNo(maxSeqNo + 1);
            gameRoomRecord.setPoints(roomPlayerDTO.getCurPoints());
            gameRoomRecord.setCreateTime(new Timestamp(System.currentTimeMillis()));
            gameRoomRecordRepository.saveAndFlush(gameRoomRecord);

            //Update Total points for each player
            GameRoomPlayerID gameRoomPlayerID = new GameRoomPlayerID();
            gameRoomPlayerID.setRoomId(roomId);
            gameRoomPlayerID.setUserId(roomPlayerDTO.getMajiangUser().getUserId());
            GameRoomPlayer gameRoomPlayer = gameRoomPlayerRepository.findById(gameRoomPlayerID).orElse(null);
            if(gameRoomPlayer != null) {
                int totalPoints = gameRoomPlayer.getPoints() + roomPlayerDTO.getCurPoints();
                roomPlayerDTO.setTotalPoints(totalPoints);
                gameRoomPlayer.setPoints(totalPoints);
                gameRoomPlayerRepository.saveAndFlush(gameRoomPlayer);
            }
            log.info("Save record for round {}, room {}  complete", maxSeqNo + 1, roomId);
        }



        //Update total points for each player
        log.info("Calculate hu details for room: {} complete", roomId);
        return roomRecordDTO;

    }
}
