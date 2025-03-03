package com.sooktin.backend.repository;

import com.sooktin.backend.domain.StorageCardMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageCardMappingRepository extends JpaRepository<StorageCardMapping, Long> {
}
