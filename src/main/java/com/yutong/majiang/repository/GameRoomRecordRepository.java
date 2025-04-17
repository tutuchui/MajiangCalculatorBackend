package com.yutong.majiang.repository;

import com.yutong.majiang.entity.GameRoomRecord;
import com.yutong.majiang.entity.GameRoomRecordID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameRoomRecordRepository extends JpaRepository<GameRoomRecord, GameRoomRecordID>{
    @Query("select max(gr.seqNo) from GameRoomRecord gr where gr.roomId = :roomId")
    Integer getMaxSeqNoByRoomId(@Param("roomId") String roomId);
}
