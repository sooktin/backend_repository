package com.sooktin.backend.service;

import com.sooktin.backend.domain.ChatRoom;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.domain.UserChatRoom;
import com.sooktin.backend.dto.chat.ChatRoomStatusUpdateRequest;
import com.sooktin.backend.repository.ChatRoomRepository;
import com.sooktin.backend.repository.UserChatRoomRepository;
import com.sooktin.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserChatRoomService {

    private final UserChatRoomRepository userChatRoomRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * 사용자가 채팅방의 멤버인지 확인
     */
    public boolean isMember(Long userId, Long roomId) {
        User user = userRepository.findById(userId).orElse(null);
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElse(null);

        if (user == null || chatRoom == null) {
            return false;
        }

        return userChatRoomRepository.findByUserAndRoom(user, chatRoom).isPresent();
    }

    /**
     * 사용자를 채팅방에 추가
     */
    @Transactional
    public void addUserToRoom(Long userId, Long roomId) {
        // 이미 멤버인지 확인
        if (isMember(userId, roomId)) {
            return;
        }

        // 사용자와 채팅방이 존재하는지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        // UserChatRoom 관계 생성 및 저장
        UserChatRoom userChatRoom = UserChatRoom.create(user, chatRoom);
        userChatRoomRepository.save(userChatRoom);
    }

    /**
     * 사용자를 채팅방에서 제거
     */
    @Transactional
    public void removeUserFromRoom(Long userId, Long roomId) {
        // 사용자와 채팅방이 존재하는지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        // 멤버인지 확인
        if (!userChatRoomRepository.findByUserAndRoom(user, chatRoom).isPresent()) {
            throw new IllegalArgumentException("사용자가 채팅방의 멤버가 아닙니다.");
        }

        // 관계 삭제
        userChatRoomRepository.deleteByUserAndRoom(user, chatRoom);

        // 채팅방에 남은 멤버가 없으면 채팅방 삭제
        List<UserChatRoom> remainingMembers = userChatRoomRepository.findByRoom(chatRoom);
        if (remainingMembers.isEmpty()) {
            chatRoomRepository.delete(chatRoom);
        }
    }

    /**
     * 채팅방의 모든 멤버 조회
     */
    public List<User> getRoomMembers(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        return userChatRoomRepository.findByRoom(chatRoom).stream()
                .map(UserChatRoom::getUser)
                .collect(Collectors.toList());
    }

    /**
     * 1:1 채팅에서 상대방 찾기
     */
    public User getOtherMember(Long roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        List<UserChatRoom> members = userChatRoomRepository.findByRoom(chatRoom);

        if (members.size() != 2) {
            throw new IllegalStateException("1:1 채팅방은 정확히 2명의 멤버가 있어야 합니다.");
        }

        return members.stream()
                .map(UserChatRoom::getUser)
                .filter(u -> !u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("상대방을 찾을 수 없습니다."));
    }

    /**
     * 사용자의 채팅방 상태 업데이트 (뮤트, 핀)
     */
    @Transactional
    public void updateRoomStatus(Long userId, Long roomId, ChatRoomStatusUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        // 멤버인지 확인
        if (!userChatRoomRepository.findByUserAndRoom(user, chatRoom).isPresent()) {
            throw new IllegalArgumentException("사용자가 채팅방의 멤버가 아닙니다.");
        }

        // 상태 업데이트
        userChatRoomRepository.updateRoomStatus(user, chatRoom, request.getIsPinned(), request.getIsMuted());
    }

    /**
     * 메시지를 읽음으로 표시
     */
    @Transactional
    public void markMessagesAsRead(Long userId, Long roomId, Long messageId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        // 멤버인지 확인
        UserChatRoom userChatRoom = userChatRoomRepository.findByUserAndRoom(user, chatRoom)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 채팅방의 멤버가 아닙니다."));

        // 읽음 처리
        userChatRoom.markAsRead(messageId);
        userChatRoomRepository.save(userChatRoom);
    }

    /**
     * 메시지 도착 시 읽지 않은 메시지 수 증가
     */
    @Transactional
    public void incrementUnreadCount(Long roomId, Long senderId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + senderId));

        userChatRoomRepository.incrementUnreadCountForAllUsersExceptSender(chatRoom, sender);
    }

    /**
     * 사용자의 전체 읽지 않은 메시지 수 조회
     */
    public int getTotalUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        Integer count = userChatRoomRepository.countTotalUnreadMessages(user);
        return count != null ? count : 0;
    }

    /**
     * 특정 채팅방의 읽지 않은 메시지 수 조회
     */
    public int getUnreadCount(Long userId, Long roomId) {
        User user = userRepository.findById(userId).orElse(null);
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElse(null);

        if (user == null || chatRoom == null) {
            return 0;
        }

        Optional<UserChatRoom> userChatRoom = userChatRoomRepository.findByUserAndRoom(user, chatRoom);
        return userChatRoom.map(UserChatRoom::getUnreadCount).orElse(0);
    }

    /**
     * 사용자의 채팅방 목록 조회 (핀 고정 및 최근 활동순)
     */
    public List<ChatRoom> getUserChatRooms(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        return userChatRoomRepository.findAllByUserOrderByStatusAndLastReadAt(user).stream()
                .map(UserChatRoom::getRoom)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 채팅방 관계 전체 조회 (정렬된 상태로)
     */
    public List<UserChatRoom> getUserChatRoomRelations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        return userChatRoomRepository.findAllByUserOrderByStatusAndLastReadAt(user);
    }
}