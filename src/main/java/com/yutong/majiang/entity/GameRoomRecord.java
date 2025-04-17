package com.yutong.majiang.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "game_room_record")
@Getter
@Setter
@IdClass(GameRoomRecordID.class)
public class GameRoomRecord {

    @Id
    @Column(name = "room_id")
    private String roomId;

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "seq_no")
    private Integer seqNo;

    @Column(name = "points")
    private Integer points;

    @Column(name = "create_time")
    private Timestamp createTime;


}
