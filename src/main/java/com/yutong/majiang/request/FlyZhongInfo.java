package com.yutong.majiang.request;

import com.yutong.majiang.response.Player;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FlyZhongInfo {
    private Player zhongPlayer;
    private Integer zhongCount;
}
