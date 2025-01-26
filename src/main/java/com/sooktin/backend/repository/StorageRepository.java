package com.sooktin.backend.repository;

import com.sooktin.backend.domain.CareerCardStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StorageRepository extends JpaRepository<CareerCardStorage,Long> {
    //Optional<CareerCardStorage> findByUserId(Long userId) 보다 41.7ms로, 이전 값인 43.3ms보다 약 1.6ms 빠름
    //그러나 시간 줄인 것보다도 JOIN 명령문이 포함되어있는데도 40ms면 빠름!!
    @Query("SELECT cs FROM CareerCardStorage  cs WHERE cs.user.id = :userId")
    Optional<CareerCardStorage> findByUserIdWithQuery(@Param("userId") Long userId);
}
