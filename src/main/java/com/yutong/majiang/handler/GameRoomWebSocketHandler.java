package com.yutong.majiang.handler;

import com.google.gson.Gson;
import com.yutong.majiang.request.RoomActionRequest;
import com.yutong.majiang.response.RoomNotifyResponse;
import com.yutong.majiang.service.RoomService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
@Slf4j
public class GameRoomWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    private RoomService roomService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // No action needed on initial connection
        log.info("Establish the session with player");
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        RoomActionRequest roomActionRequest = new Gson().fromJson(payload, RoomActionRequest.class);
        if("join".equals(roomActionRequest.getAction())) {
            String roomId = roomActionRequest.getRoomDetail().getRoomId();
            String userId = roomActionRequest.getRoomDetail().getUserId();
            log.info("Received add room request for player {} and room {}", userId, roomId);

            roomService.addUser(roomId, userId);
            addPlayerToRoom(roomId, session);
            notifyPlayersAdded(roomId, userId);
        }else if("close".equals(roomActionRequest.getAction())) {
            String roomId = roomActionRequest.getRoomDetail().getRoomId();
            roomService.closeRoom(roomId);
            notifyRoomClosed(roomId);
        }else if("calculate".equals(roomActionRequest.getAction())) {

        }


    }

    private void addPlayerToRoom(String roomId, WebSocketSession session) {
        roomSessions.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    private void notifyRoomClosed(String roomId) throws Exception {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                log.info("Notify to close room for: {}", roomId);
                RoomNotifyResponse roomNotifyResponse = new RoomNotifyResponse();
                roomNotifyResponse.setRoomId(roomId);
                roomNotifyResponse.setAction("close");
                session.sendMessage(new TextMessage(new Gson().toJson(roomNotifyResponse)));
            }
        }
        roomSessions.remove(roomId);
    }

    private void notifyPlayersAdded(String roomId, String userId) throws Exception {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                log.info("Notify to refresh room list for: {}", roomId);
                RoomNotifyResponse roomNotifyResponse = new RoomNotifyResponse();
                roomNotifyResponse.setRoomId(roomId);
                roomNotifyResponse.setUserId(userId);
                roomNotifyResponse.setAction("join");
                session.sendMessage(new TextMessage(new Gson().toJson(roomNotifyResponse)));
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket closed due to error", exception);
        try {
            session.close();
        } catch (IOException e) {
            log.error("Error occurred during close the session", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.warn("WebSocket Closed: " + session.getId() + " Status: " + status);
    }
}
