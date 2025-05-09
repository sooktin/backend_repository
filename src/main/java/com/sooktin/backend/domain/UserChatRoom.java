package com.sooktin.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_chat_room",uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "room_id"}))
public class UserChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id",nullable = false)
    private ChatRoom room;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    @Column(name = "unread_count", nullable = false)
    private Integer unreadCount = 0;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "is_muted")
    private Boolean isMuted = false;

    @Column(name = "is_pinned")
    private Boolean isPinned = false;

    /**
     * 사용자와 채팅방으로 객체 생성하는 편의 메서드
     */
    public static UserChatRoom create(User user, ChatRoom chatRoom) {
        UserChatRoom userChatRoom = new UserChatRoom();
        userChatRoom.setUser(user);
        userChatRoom.setRoom(chatRoom);
        userChatRoom.setUnreadCount(0);
        userChatRoom.setJoinedAt(LocalDateTime.now());
        userChatRoom.setIsMuted(false);
        userChatRoom.setIsPinned(false);
        return userChatRoom;
    }

    /**
     * ID로 객체를 생성하는 편의 메서드 (추가됨)
     */
    public static UserChatRoom create(Long userId, Long roomId, User user, ChatRoom chatRoom) {
        UserChatRoom userChatRoom = new UserChatRoom();
        userChatRoom.setUser(user);
        userChatRoom.setRoom(chatRoom);
        userChatRoom.setUnreadCount(0);
        userChatRoom.setJoinedAt(LocalDateTime.now());
        userChatRoom.setIsMuted(false);
        userChatRoom.setIsPinned(false);
        return userChatRoom;
    }

    /**
     * 읽음 처리 메서드
     */
    public void markAsRead(Long messageId) {
        this.lastReadMessageId = messageId;
        this.lastReadAt = LocalDateTime.now();
        this.unreadCount = 0;
    }

    /**
     * 읽지 않은 메시지 카운트 증가 메서드
     */
    public void incrementUnreadCount() {
        this.unreadCount = (this.unreadCount == null) ? 1 : this.unreadCount + 1;
    }

    /**
     * 뮤트 상태 변경
     */
    public void setMuted(boolean muted) {
        this.isMuted = muted;
    }

    /**
     * 핀 상태 변경
     */
    public void setPinned(boolean pinned) {
        this.isPinned = pinned;
    }

    /**
     * User ID 가져오기 편의 메서드
     */
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    /**
     * Room ID 가져오기 편의 메서드
     */
    public Long getRoomId() {
        return room != null ? room.getId() : null;
    }
}