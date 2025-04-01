package com.yutong.majiang.service;

import com.yutong.majiang.constant.Constants;
import com.yutong.majiang.entity.MajiangUser;
import com.yutong.majiang.repository.MajiangUserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

    private MajiangUserRepository majiangUserRepository;

    public void save(String  userId) {
       if(majiangUserRepository.findById(userId).isEmpty()) {
           MajiangUser majiangUser = new MajiangUser();
           majiangUser.setUserId(userId);
           majiangUser.setNickname(Constants.DEFAULT_NICKNAME);
           majiangUser.setCreateDate(new Timestamp(System.currentTimeMillis()));
           majiangUserRepository.saveAndFlush(majiangUser);
           log.info("Create user successful for {}", majiangUser.getUserId());
       }
    }

    public void update(MajiangUser  majiangUser) {
        majiangUserRepository.saveAndFlush(majiangUser);
        log.info("Update user successful for {}", majiangUser.getUserId());
    }

    public MajiangUser getUserDetailById(String userId) {
        if(majiangUserRepository.findById(userId).isPresent()) {
            return majiangUserRepository.findById(userId).get();
        }else {
            return null;
        }
    }

    public String saveAvatar(MultipartFile file, String userId) throws IOException {
        log.info("start save user avatar");
        String targetPath = Constants.ASSET_FOLDER;
        MajiangUser majiangUser = majiangUserRepository.findById(userId).get();
        String fileType = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String fileName = userId + "_Avatar" + fileType;
        log.info("fileName: {}, file path: {}", fileName, targetPath);
        Files.copy(file.getInputStream(), Path.of(targetPath + "/" + fileName), StandardCopyOption.REPLACE_EXISTING);
        majiangUser.setAvatarUrl("/asset/" + fileName);
        majiangUserRepository.saveAndFlush(majiangUser);
        return  "/asset/" + fileName;
    }

}
