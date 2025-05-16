package com.sooktin.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sooktin.backend.domain.ChatMessage;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, Long>{
    List<ChatMessage> findByRoomIdOrderByTimestampDesc(String roomId);

    default List<ChatMessage> findLatestMessages(String roomId, int limit) {
        return findByRoomIdOrderByTimestampDesc(roomId).stream()
                .limit(limit)
                .toList();
    }

    Optional<ChatMessage> findTopByRoomIdOrderByTimestampDesc(String roomId);

    //마지막 특정 메시지ID 이후 메시지 개수
    int countByRoomIdAndMessageIdGreaterThan(String roomId, Long messageId);

    //특정 사용자가 마지막으로 읽은 후 모든 메시지 조회
    List<ChatMessage> findByRoomIdAndMessageIdGreaterThanOrderByTimestampAsc(String roomId, Long messageId);
    
}
