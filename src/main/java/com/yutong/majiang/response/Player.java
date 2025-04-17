package com.yutong.majiang.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player {
    private String userId;
    private String nickname;
    private String avatarUrl;
    private Boolean isHost;
    private Integer points;
}
