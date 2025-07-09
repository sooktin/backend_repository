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
@AllArgsConstructor
public class ChatRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;


    private LocalDateTime lastMessageAt;
    private String lastMessagePreview;

    // 생성자 추가
    public ChatRoom() {
        this.lastMessageAt = LocalDateTime.now();
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