package com.yutong.majiang.request;

import com.yutong.majiang.response.Player;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HuDetail {
    private Player huPlayer;
    private Integer basePoint;
    private Integer maCount;
    private Boolean isGangKai;
    private Boolean fourZhongWin;
    private List<FlyZhongInfo> flyZhongInfoList;
    private List<GangInfo> gangInfoList;
}
