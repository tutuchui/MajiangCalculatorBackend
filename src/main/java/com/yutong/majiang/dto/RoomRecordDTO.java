package com.yutong.majiang.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
public class RoomRecordDTO {
    private List<RoomPlayerDTO> playerRecordList;
    private Timestamp createTime;
}
