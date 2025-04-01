package com.yutong.majiang.request;

import com.yutong.majiang.response.Player;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GangInfo {
    private Player winPlayer;
    private Player losePlayer;
    private Integer gangCount;
}
