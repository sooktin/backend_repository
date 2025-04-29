package com.sooktin.backend.repository;

import com.sooktin.backend.domain.CareerCardStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StorageRepository extends JpaRepository<CareerCardStorage,Long> {
    @Query("SELECT s.id, s FROM CareerCardStorage s LEFT JOIN FETCH s.cardMappings m LEFT JOIN FETCH m.careerCard WHERE s.user.id = :userId")
    List<Object[]> findByUserIdWithQuery(@Param("userId") Long userId);

    // getCardsFromStorage용: card_id 리스트만 조회
    @Query("SELECT m.careerCard, u.nickname " +
            "FROM CareerCardStorage  s " +
            "JOIN s.cardMappings m " +
            "JOIN m.careerCard c " +
            "JOIN c.user u " +
            "WHERE s.user.id = :userId"
    )
    List<Object[]> findCareerCardsByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM CareerCardStorage s WHERE s.user.id = :userId")
    Optional<CareerCardStorage> findByUserId(@Param("userId") Long userId); // Optional<CareerCardStorage>
}
