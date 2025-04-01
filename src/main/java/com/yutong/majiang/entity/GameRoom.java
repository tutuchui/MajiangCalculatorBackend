package com.yutong.majiang.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "game_room", schema = "majiang_calculator")
public class GameRoom {
    @Id
    @Column(name = "room_id", nullable = false)
    private String roomId;

    @Column(name = "start_time", nullable = false)
    private Timestamp startTime;

    @Column(name = "end_time")
    private Timestamp endTime;

    @Column(name = "status")
    private Integer status;

    @Column(name = "creator")
    private String creator;

}