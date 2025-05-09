package com.sooktin.backend.repository;

import com.sooktin.backend.domain.ChatRoom;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.domain.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {

    /**
     * 특정 사용자의 특정 채팅방 관계 조회
     */
    Optional<UserChatRoom> findByUserAndRoom(User user, ChatRoom chatRoom);

    /**
     * 사용자 ID와 채팅방 ID로 관계 조회 (추가됨)
     */
    @Query("SELECT ucr FROM UserChatRoom ucr WHERE ucr.user.id = :userId AND ucr.room.id = :roomId")
    Optional<UserChatRoom> findByUserIdAndRoomId(@Param("userId") Long userId, @Param("roomId") Long roomId);

    /**
     * 특정 사용자의 모든 채팅방 관계 조회
     */
    List<UserChatRoom> findByUser(User user);

    /**
     * 특정 채팅방의 모든 사용자 관계 조회
     */
    List<UserChatRoom> findByRoom(ChatRoom chatRoom);

    /**
     * 채팅방 ID로 모든 사용자 관계 조회 (추가됨)
     */
    @Query("SELECT ucr FROM UserChatRoom ucr WHERE ucr.room.id = :roomId")
    List<UserChatRoom> findByRoomId(@Param("roomId") Long roomId);

    /**
     * 특정 사용자의 읽지 않은 메시지가 있는 채팅방 조회
     */
    List<UserChatRoom> findByUserAndUnreadCountGreaterThan(User user, Integer unreadCount);

    /**
     * 특정 사용자의 모든 읽지 않은 메시지 수 합계 조회
     */
    @Query("SELECT SUM(ucr.unreadCount) FROM UserChatRoom ucr WHERE ucr.user = :user")
    Integer countTotalUnreadMessages(@Param("user") User user);

    /**
     * 새 메시지 도착 시 모든 참여자의 읽지 않은 메시지 수 증가 (발신자 제외)
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserChatRoom ucr SET ucr.unreadCount = ucr.unreadCount + 1 " +
            "WHERE ucr.room = :chatRoom AND ucr.user != :sender")
    void incrementUnreadCountForAllUsersExceptSender(@Param("chatRoom") ChatRoom chatRoom, @Param("sender") User sender);

    /**
     * 특정 사용자의 채팅방에서 나가기 (삭제)
     */
    void deleteByUserAndRoom(User user, ChatRoom chatRoom);

    /**
     * 사용자 ID와 채팅방 ID로 관계 삭제 (추가됨)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserChatRoom ucr WHERE ucr.user.id = :userId AND ucr.room.id = :roomId")
    void deleteByUserIdAndRoomId(@Param("userId") Long userId, @Param("roomId") Long roomId);

    /**
     * 특정 메시지 ID까지의 모든 메시지를 읽음 처리
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserChatRoom ucr SET ucr.lastReadMessageId = :messageId, " +
            "ucr.lastReadAt = CURRENT_TIMESTAMP, ucr.unreadCount = 0 " +
            "WHERE ucr.user = :user AND ucr.room = :chatRoom")
    void markAsReadUpToMessageId(@Param("user") User user, @Param("chatRoom") ChatRoom chatRoom, @Param("messageId") Long messageId);

    /**
     * 사용자가 핀한 채팅방 관계 목록 조회
     */
    List<UserChatRoom> findByUserAndIsPinnedTrueOrderByLastReadAtDesc(User user);

    /**
     * 1:1 채팅에서 두 사용자가 공유하는 채팅방 관계 찾기
     */
    @Query("SELECT ucr.room FROM UserChatRoom ucr WHERE ucr.user = :user1 AND ucr.room IN " +
            "(SELECT ucr2.room FROM UserChatRoom ucr2 WHERE ucr2.user = :user2)")
    List<ChatRoom> findSharedChatRooms(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * 읽지 않은 메시지 카운트 초기화
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserChatRoom ucr SET ucr.unreadCount = 0 " +
            "WHERE ucr.user = :user AND ucr.room = :chatRoom")
    void resetUnreadCount(@Param("user") User user, @Param("chatRoom") ChatRoom chatRoom);

    /**
     * 특정 사용자의, 특정 채팅방의 상태 업데이트 (핀, 뮤트)
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserChatRoom ucr SET " +
            "ucr.isPinned = CASE WHEN :isPinned IS NULL THEN ucr.isPinned ELSE :isPinned END, " +
            "ucr.isMuted = CASE WHEN :isMuted IS NULL THEN ucr.isMuted ELSE :isMuted END " +
            "WHERE ucr.user = :user AND ucr.room = :chatRoom")
    void updateRoomStatus(@Param("user") User user, @Param("chatRoom") ChatRoom chatRoom,
                          @Param("isPinned") Boolean isPinned, @Param("isMuted") Boolean isMuted);

    /**
     * 특정 사용자의 모든 채팅방 관계 찾기 (핀 우선, 최근 활동순)
     */
    @Query("SELECT ucr FROM UserChatRoom ucr " +
            "WHERE ucr.user = :user " +
            "ORDER BY ucr.isPinned DESC, ucr.lastReadAt DESC")
    List<UserChatRoom> findAllByUserOrderByStatusAndLastReadAt(@Param("user") User user);
}