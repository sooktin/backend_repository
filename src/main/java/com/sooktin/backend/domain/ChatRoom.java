package com.sooktin.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private LocalDateTime createdAt;

    private LocalDateTime lastMessageAt;
    private String lastMessagePreview;

    // 생성자 추가
    public ChatRoom(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        this.lastMessageAt = createdAt;
    }

    public void updateLastMessage(String content, LocalDateTime timestamp) {
        this.lastMessageAt = timestamp;
        if (content != null) {
            this.lastMessagePreview = content.length() > 30
                    ? content.substring(0, 27) + "..."
                    : content;
        }
    }

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserChatRoom> userChatRooms = new ArrayList<>();
}