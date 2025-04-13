package com.yutong.majiang.service;

import com.yutong.majiang.constant.Constants;
import com.yutong.majiang.dto.RoomPlayerDTO;
import com.yutong.majiang.entity.GameRoom;
import com.yutong.majiang.entity.GameRoomPlayer;
import com.yutong.majiang.entity.GameRoomPlayerID;
import com.yutong.majiang.entity.MajiangUser;
import com.yutong.majiang.repository.GameRoomPlayerRepository;
import com.yutong.majiang.repository.GameRoomRepository;
import com.yutong.majiang.request.HuDetail;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class RoomService {

    private GameRoomRepository gameRoomRepository;

    private GameRoomPlayerRepository gameRoomPlayerRepository;

    private UserService userService;

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

    public void calculate(HuDetail huDetail, String roomId) {

    }
}
