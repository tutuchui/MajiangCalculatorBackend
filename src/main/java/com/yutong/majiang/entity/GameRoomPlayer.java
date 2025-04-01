package com.yutong.majiang.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Table(name = "GAME_ROOM_PLAYER")
@Entity
@Getter
@Setter
@IdClass(GameRoomPlayerID.class)
public class GameRoomPlayer {
    @Id
    @Column(name = "ROOM_ID")
    private String roomId;

    @Id
    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "IS_HOST")
    private Integer isHost;

    @Column(name = "JOINED_DATE")
    private Timestamp joinedDate;

}
