package com.yutong.majiang.request;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateQRCodeRequest {
    private String scene;
    private String page;
    private String width;
    @SerializedName("check_path")
    private Boolean checkPath;
}
