package com.yutong.majiang.response;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WechatOpenIdResponse {
    private String openid;
    @SerializedName("session_key")
    private String sessionKey;
}
