package com.yutong.majiang.repository;

import com.yutong.majiang.entity.GameRoomPlayer;
import com.yutong.majiang.entity.GameRoomPlayerID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRoomPlayerRepository extends JpaRepository<GameRoomPlayer, GameRoomPlayerID> {
    List<GameRoomPlayer> findGameRoomPlayersByRoomId(String roomId);
}
