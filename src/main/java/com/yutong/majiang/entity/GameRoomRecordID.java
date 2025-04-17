package com.yutong.majiang.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class GameRoomRecordID implements Serializable {
    @Id
    @Column(name = "room_id")
    private String roomId;

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "seq_no")
    private Integer seqNo;
}
