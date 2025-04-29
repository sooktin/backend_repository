package com.sooktin.backend.repository;

import com.sooktin.backend.domain.CareerCardStorage;
import com.sooktin.backend.domain.StorageCardMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StorageCardMappingRepository extends JpaRepository<StorageCardMapping, Long> {
    Optional<StorageCardMapping> findByCareerCardStorageAndCareerCardId(CareerCardStorage careerCardStorage, Long cardId);
}
