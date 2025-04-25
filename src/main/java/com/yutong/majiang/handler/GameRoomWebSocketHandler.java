package com.yutong.majiang.handler;

import com.google.gson.Gson;
import com.yutong.majiang.constant.Constants;
import com.yutong.majiang.dto.RoomRecordDTO;
import com.yutong.majiang.request.HuDetail;
import com.yutong.majiang.request.RoomActionRequest;
import com.yutong.majiang.response.GameRecordResponse;
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
import java.net.URI;
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
        String roomId = "";
        URI uri = session.getUri(); // ws://.../ws/game-room?userId=abc123
        if (uri == null) {
            return;
        }
        String query = uri.getQuery(); // room=abc123
        if (query == null) {
            return;
        }
        for (String param : query.split("&")) {
            String[] kv = param.split("=");
            if (kv.length == 2 && kv[0].equals("roomId")) {
                roomId =  kv[1];
                roomSessions.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);
                log.info("add player session to room {}", roomId);
            }
        }
        log.info("Establish the session with player");
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        RoomActionRequest roomActionRequest = new Gson().fromJson(payload, RoomActionRequest.class);
        if(Constants.ROOM_ACTION_JOIN.equals(roomActionRequest.getAction())) {
            String roomId = roomActionRequest.getRoomDetail().getRoomId();
            String userId = roomActionRequest.getRoomDetail().getUserId();
            log.info("Received add room request for player {} and room {}", userId, roomId);
            roomService.addUser(roomId, userId);
            addPlayerToRoom(roomId, session);
            notifyPlayersAdded(roomId, userId);
        }else if(Constants.ROOM_ACTION_CLOSE.equals(roomActionRequest.getAction())) {
            String roomId = roomActionRequest.getRoomDetail().getRoomId();
            roomService.closeRoom(roomId);
            notifyRoomClosed(roomId);
        }else if(Constants.ROOM_ACTION_LEAVE.equals(roomActionRequest.getAction())) {
            String roomId = roomActionRequest.getRoomDetail().getRoomId();
            String userId = roomActionRequest.getRoomDetail().getUserId();
            roomService.removeUser(roomId, userId);
            notifyPlayerLeave(roomId, userId, session);
            log.info("remove player {} from room {}", userId, roomId);
        }else if(Constants.ROOM_ACTION_CALCULATE.equals(roomActionRequest.getAction())) {
            String roomId = roomActionRequest.getRoomDetail().getRoomId();
            HuDetail huDetail = roomActionRequest.getHuDetail();
            RoomRecordDTO roomRecordDTO = roomService.calculate(huDetail, roomId);
            notifyCalculateResult(roomId, roomRecordDTO);
            log.info("Calculate game result complete");
        }


    }

    private void addPlayerToRoom(String roomId, WebSocketSession session) {
        roomSessions.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    private void notifyRoomClosed(String roomId) throws Exception {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                log.info("Close sessions after close room");
                closeSession(session);
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

    private void notifyCalculateResult(String roomId, RoomRecordDTO roomRecordDTO) throws Exception {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                log.info("Send game record result for room: {}", roomId);
                GameRecordResponse gameRecordResponse = new GameRecordResponse();
                gameRecordResponse.setRoomId(roomId);
                gameRecordResponse.setRoomRecordDTO(roomRecordDTO);
                gameRecordResponse.setAction("calculate");
                session.sendMessage(new TextMessage(new Gson().toJson(gameRecordResponse)));
            }
        }
    }

    private void notifyPlayerLeave(String roomId, String userId, WebSocketSession session) throws Exception {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            closeSession(session);
            for (WebSocketSession curSession : sessions) {
                log.info("Notify to refresh room list for: {}", roomId);
                RoomNotifyResponse roomNotifyResponse = new RoomNotifyResponse();
                roomNotifyResponse.setRoomId(roomId);
                roomNotifyResponse.setUserId(userId);
                roomNotifyResponse.setAction("leave");
                curSession.sendMessage(new TextMessage(new Gson().toJson(roomNotifyResponse)));
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket closed due to error", exception);
        closeSession(session);
    }

    public void closeSession(WebSocketSession session) {
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
