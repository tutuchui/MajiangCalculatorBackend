package com.yutong.majiang.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomActionRequest {
    private String action;
    private RoomDetail roomDetail;
    private HuDetail huDetail;

    @Getter
    @Setter
    public static class RoomDetail {
        private String roomId;
        private String userId;
    }


}
