package com.yutong.majiang.dto;

import com.yutong.majiang.entity.MajiangUser;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomPlayerDTO {
    private MajiangUser majiangUser;
    private Boolean isHost;
}
