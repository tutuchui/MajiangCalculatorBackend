package com.yutong.majiang.repository;

import com.yutong.majiang.entity.GameRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRoomRepository extends JpaRepository<GameRoom, String> {

    List<GameRoom> findByRoomIdAndStatus(String roomId, Integer status);
}
