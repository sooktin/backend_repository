package com.sooktin.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@Table(name = "career_card_storage")
@NoArgsConstructor
@AllArgsConstructor
public class CareerCardStorage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_storage_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "careerCardStorage",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StorageCardMapping> cardMappings = new ArrayList<>();


}