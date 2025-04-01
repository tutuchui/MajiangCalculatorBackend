package com.yutong.majiang.interfaces;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yutong.majiang.constant.Constants;
import com.yutong.majiang.exception.GenerateQRCodeException;
import com.yutong.majiang.exception.GetOpenIDException;
import com.yutong.majiang.request.CreateQRCodeRequest;
import com.yutong.majiang.response.WechatAccessTokenResponse;
import com.yutong.majiang.response.WechatOpenIdResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Component
@Slf4j
public class WechatProxy {

    private final RestTemplate restTemplate = new RestTemplate();

    public String getOpenId(String code) throws GetOpenIDException {
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + Constants.APP_ID +
                "&secret=" + Constants.SECRET_KEY +
                "&js_code=" + code +
                "&grant_type=authorization_code";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        // Check if OpenID exists
        if (response.getStatusCode().is2xxSuccessful()) {
            WechatOpenIdResponse wechatOpenIdResponse = new Gson().fromJson(response.getBody(), WechatOpenIdResponse.class);
            return wechatOpenIdResponse.getOpenid(); // Contains openid, session_key, and possible unionid
        } else {
            throw new GetOpenIDException();
        }

    }

    public String getAccessToken() throws GetOpenIDException {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential" +
                "&appid=" + Constants.APP_ID + "&secret=" + Constants.SECRET_KEY;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            WechatAccessTokenResponse wechatAccessTokenResponse = new Gson().fromJson(response.getBody(), WechatAccessTokenResponse.class);
            return wechatAccessTokenResponse.getAccessToken(); // Contains openid, session_key, and possible unionid
        } else {
            throw new GetOpenIDException();
        }
    }

    public String getShareRoomQRCode(String roomId) {
        try {
            String accessToken = this.getAccessToken();
            String url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + accessToken;
            CreateQRCodeRequest createQRCodeRequest = new CreateQRCodeRequest();
            createQRCodeRequest.setScene("roomId=" + roomId);
            createQRCodeRequest.setPage("pages/room/room");
            createQRCodeRequest.setWidth("200");
            createQRCodeRequest.setCheckPath(false);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json; charset=utf-8");

            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            HttpEntity<String> request = new HttpEntity<>(gson.toJson(createQRCodeRequest), headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, request, byte[].class);

            if(response.getStatusCode().is2xxSuccessful()) {
                log.info("Generate QR Code successful for room {}", roomId);
                return Base64.getEncoder().encodeToString(response.getBody());
            }else {
                throw new GenerateQRCodeException();
            }

        }catch (Exception e){
            log.error("Generate QR Code failure", e);
            throw new GenerateQRCodeException();
        }


    }
}
