package com.yutong.majiang.controller;

import com.yutong.majiang.dto.RoomPlayerDTO;
import com.yutong.majiang.entity.MajiangUser;
import com.yutong.majiang.exception.GenerateQRCodeException;
import com.yutong.majiang.interfaces.WechatProxy;
import com.yutong.majiang.request.CreateRoomRequest;
import com.yutong.majiang.response.*;
import com.yutong.majiang.service.RoomService;
import com.yutong.majiang.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@AllArgsConstructor
public class RoomController {

    private final RoomService roomService;

    private final WechatProxy wechatProxy;

    @PostMapping("/room")
    public ResponseEntity<Object> createRoom(@RequestBody CreateRoomRequest request) {
        log.info("Received create room request: {}", request.getCreator());
        String roomId = roomService.createRoom(request.getCreator());
        return ResponseEntity.ok(new CreateRoomResponse(roomId));
    }


    @GetMapping("room/{roomId}")
    public ResponseEntity<Object> getPlayersById(@PathVariable String roomId) {
        log.info("Received get players by room id: {}", roomId);
        List<RoomPlayerDTO> majiangUserList = roomService.getUsersByRoomId(roomId);
        return ResponseEntity.ok(new RoomPlayerResponse(majiangUserList));
    }

    @GetMapping("exist/room/{roomId}")
    public ResponseEntity<Object> existRoom(@PathVariable String roomId) {
        log.info("Received check exist room request: {}", roomId);
        boolean isExist = roomService.isRoomExist(roomId.toUpperCase());
        return ResponseEntity.ok(new CheckRoomExistResponse(isExist));
    }

    @GetMapping("qrcode/room/{roomId}")
    public ResponseEntity<Object> generateShareQRCode(@PathVariable String roomId) {
        log.info("Received get share QR code by room id: {}", roomId);
        try {
            String imageData = wechatProxy.getShareRoomQRCode(roomId);
            return ResponseEntity.ok(new GenerateQRCodeResponse(imageData));
        }catch (GenerateQRCodeException e) {
            return ResponseEntity.internalServerError().body("Generate QR code error");
        }

    }
}
