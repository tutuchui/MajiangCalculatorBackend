package com.yutong.majiang.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {
    private String nickname;
    private String avatarUrl;
}
