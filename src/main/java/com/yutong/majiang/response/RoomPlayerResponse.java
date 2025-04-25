package com.yutong.majiang.response;

import com.yutong.majiang.dto.RoomPlayerDTO;
import com.yutong.majiang.entity.MajiangUser;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RoomPlayerResponse {
    private List<Player> playerList;

    public RoomPlayerResponse(List<RoomPlayerDTO> majiangUserList) {
        List<Player> playerList = new ArrayList<>();
        for(RoomPlayerDTO roomPlayerDTO : majiangUserList) {
            Player player = new Player();
            MajiangUser majiangUser = roomPlayerDTO.getMajiangUser();
            player.setUserId(majiangUser.getUserId());
            player.setNickname(majiangUser.getNickname());
            player.setAvatarUrl(majiangUser.getAvatarUrl());
            player.setIsHost(roomPlayerDTO.getIsHost());
            player.setPoints(roomPlayerDTO.getTotalPoints());
            playerList.add(player);
        }
        this.playerList = playerList;
    }
}
