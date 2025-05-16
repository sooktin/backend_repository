package com.sooktin.backend.service;

import com.sooktin.backend.domain.ChatMessage;
import com.sooktin.backend.domain.ChatRoom;
import com.sooktin.backend.domain.User;
import com.sooktin.backend.domain.UserChatRoom;
import com.sooktin.backend.dto.chat.*;
import com.sooktin.backend.repository.ChatRoomRepository;
import com.sooktin.backend.repository.UserChatRoomRepository;
import com.sooktin.backend.repository.UserRepository;
import com.sooktin.backend.repository.ChatRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final ChatRepository chatRepository;

    /**
     * 채팅방 참여자 목록 조회
     */
    public Set<User> getParticipants(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        List<UserChatRoom> relations = userChatRoomRepository.findByRoom(chatRoom);

        return relations.stream()
                .map(UserChatRoom::getUser)
                .collect(Collectors.toSet());
    }

    /**
     * 사용자가 채팅방에 참여 중인지 확인
     */
    public boolean hasParticipant(Long roomId, Long userId) {
        return userChatRoomRepository.findByUserIdAndRoomId(userId, roomId).isPresent();
    }

    /**
     * 채팅방에 참여자 추가
     */
    @Transactional
    public void addParticipant(Long roomId, Long userId) {
        // 이미 참여 중인지 확인
        if (hasParticipant(roomId, userId)) {
            return; // 이미 참여 중이면 무시
        }

        // 채팅방 및 사용자 존재 확인
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // UserChatRoom 생성 및 저장
        UserChatRoom userChatRoom = UserChatRoom.create(user, chatRoom);
        userChatRoomRepository.save(userChatRoom);
    }

    /**
     * 채팅방에서 참여자 제거
     */
    @Transactional
    public void removeParticipant(Long roomId, Long userId) {
        userChatRoomRepository.deleteByUserIdAndRoomId(userId, roomId);

        // 참여자가 없으면 채팅방 삭제 검토
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        if (userChatRoomRepository.findByRoom(chatRoom).isEmpty()) {
            chatRoomRepository.deleteById(roomId);
        }
    }

    /**
     * 1:1 채팅에서 상대방 참여자 찾기
     */
    public User getOtherParticipant(Long roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        List<UserChatRoom> relations = userChatRoomRepository.findByRoom(chatRoom);

        return relations.stream()
                .map(UserChatRoom::getUser)
                .filter(user -> !user.getId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 새 채팅방을 생성합니다.
     */
    @Transactional
    public ChatRoom createChatRoom(ChatRoom chatRoom) {
        return chatRoomRepository.save(chatRoom);
    }

    /**
     * ID로 채팅방을 조회합니다.
     */
    @Transactional(readOnly = true)
    public Optional<ChatRoom> getChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId);
    }
}