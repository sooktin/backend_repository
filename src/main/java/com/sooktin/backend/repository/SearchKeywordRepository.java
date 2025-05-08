package com.sooktin.backend.repository;

import com.sooktin.backend.domain.SearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {

    // 특정 키워드와 기간으로 검색
    Optional<SearchKeyword> findByKeywordAndPeriodAndPeriodKey(String keyword, String period, String periodKey);

    // 특정 기간의 모든 키워드 조회
    List<SearchKeyword> findByPeriodAndPeriodKeyOrderByScoreDesc(String period, String periodKey);

    // 특정 기간 동안의 인기 검색어 (상위 N개)
    @Query("SELECT sk FROM SearchKeyword sk WHERE sk.searchDate BETWEEN :startDate AND :endDate ORDER BY sk.score DESC LIMIT :limit")
    List<SearchKeyword> findTopKeywordsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("limit") int limit
    );

    @Query("SELECT sk FROM SearchKeyword sk WHERE sk.keyword = :keyword AND sk.period = :period AND sk.searchDate BETWEEN :startDate AND :endDate ORDER BY sk.searchDate")
    List<SearchKeyword> findKeywordTrendByPeriod(
            @Param("keyword") String keyword,
            @Param("period") String period,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}