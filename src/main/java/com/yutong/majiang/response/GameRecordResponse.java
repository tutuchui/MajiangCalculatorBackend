package com.yutong.majiang.response;

import com.yutong.majiang.dto.RoomRecordDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameRecordResponse {
    private String roomId;
    private RoomRecordDTO roomRecordDTO;
    private String action;
}
