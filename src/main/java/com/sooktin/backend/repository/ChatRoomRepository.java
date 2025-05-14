package com.sooktin.backend.repository;

import com.sooktin.backend.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 특정 사용자가 참여 중인 채팅방 목록 조회
     * UserChatRoom 테이블을 조인하여 조회
     */
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.userChatRooms ucr WHERE ucr.user.id = :userId")
    List<ChatRoom> findRoomsByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자가 참여 중인 채팅방 중 최근 활동 순으로 정렬
     */
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.userChatRooms ucr WHERE ucr.user.id = :userId ORDER BY cr.lastMessageAt DESC")
    List<ChatRoom> findRoomsByUserIdOrderByLastActivityDesc(@Param("userId") Long userId);


}