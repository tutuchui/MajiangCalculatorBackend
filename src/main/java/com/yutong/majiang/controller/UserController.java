package com.yutong.majiang.controller;

import com.yutong.majiang.entity.MajiangUser;
import com.yutong.majiang.exception.GetOpenIDException;
import com.yutong.majiang.interfaces.WechatProxy;
import com.yutong.majiang.request.MajiangOpenIdRequest;
import com.yutong.majiang.request.UpdateUserRequest;
import com.yutong.majiang.response.ErrorResponse;
import com.yutong.majiang.response.MajingOpenIdResponse;
import com.yutong.majiang.response.UserDetailResponse;
import com.yutong.majiang.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@CrossOrigin
@AllArgsConstructor
@Slf4j
public class UserController {

    private WechatProxy wechatProxy;

    private UserService userService;

    @PostMapping("/user/openid")
    public ResponseEntity<Object> getOpenid(@RequestBody MajiangOpenIdRequest majiangOpenIdRequest) {
        log.info("Received get openid request");
        String openId = "";
        try {
            openId = wechatProxy.getOpenId(majiangOpenIdRequest.getCode());
            log.info("Get openId: {} from wechat successfully", openId);
        }catch (GetOpenIDException e) {
            log.error("Error retrieving openid from wechat, generate openid by the system");
            openId = UUID.randomUUID().toString();
        }
        userService.save(openId);
        return ResponseEntity.ok(new MajingOpenIdResponse(openId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Object> getUserDetail(@PathVariable String userId) {
        log.info("Received get user detail request for {}", userId);
        MajiangUser majiangUser = userService.getUserDetailById(userId);
        if(majiangUser == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("invalid", "invalid user id"));
        }
        UserDetailResponse userDetailResponse = new UserDetailResponse();;
        userDetailResponse.setNickname(majiangUser.getNickname());
        userDetailResponse.setAvatarUrl(majiangUser.getAvatarUrl());
        return ResponseEntity.ok(userDetailResponse);
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<Object> updateUserAvatar(@PathVariable String userId, @RequestBody UpdateUserRequest updateUserRequest) {
        log.info("Received update user avatar request for {}", userId);
        MajiangUser majiangUser = userService.getUserDetailById(userId);
        if(majiangUser == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("invalid", "invalid user id"));
        }
        if(!StringUtils.isEmpty(updateUserRequest.getAvatarUrl())) {
            majiangUser.setAvatarUrl(updateUserRequest.getAvatarUrl());
        }
        if(!StringUtils.isEmpty(updateUserRequest.getNickname())) {
            majiangUser.setNickname(updateUserRequest.getNickname());
        }
        userService.update(majiangUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/avatar/{userId}")
    public ResponseEntity<Object> uploadAvatar(@PathVariable String userId, @RequestParam("file") MultipartFile file)
    throws IOException {
        log.info("Received upload avatar request for {}", userId);
        String filePath = userService.saveAvatar(file, userId);
        return ResponseEntity.ok(new UserDetailResponse("", filePath));
    }
}
