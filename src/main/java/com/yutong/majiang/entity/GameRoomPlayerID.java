package com.yutong.majiang.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class GameRoomPlayerID implements Serializable {
    @Id
    @Column(name = "ROOM_ID")
    private String roomId;

    @Id
    @Column(name = "USER_ID")
    private String userId;
}
