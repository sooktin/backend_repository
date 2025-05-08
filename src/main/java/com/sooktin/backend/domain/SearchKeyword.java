package com.sooktin.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "search_keywords",
        indexes = {
                @Index(name = "idx_keyword", columnList = "keyword"),
                @Index(name = "idx_search_date", columnList = "searchDate")
        })
public class SearchKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String keyword;

    @Column(nullable = false)
    private int hourlyCount;

    @Column(nullable = false)
    private int dailyCount;

    @Column(nullable = false)
    private int weeklyCount;

    @Column(nullable = false)
    private int totalCount;

    @Column(nullable = false)
    private double score;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime searchDate;

    @Column(length = 10)
    private String period; // hourly, daily, weekly 중

    @Column(length = 20)
    private String periodKey; // yyyyMMddHH, yyyyMMdd, yyyyww 형식의 기간 식별자
}