package com.yutong.majiang.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomNotifyResponse {
    private String action;
    private String roomId;
    private String userId;
}
