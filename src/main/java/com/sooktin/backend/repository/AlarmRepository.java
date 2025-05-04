package com.sooktin.backend.repository;

import com.sooktin.backend.domain.Alarm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    Page<Alarm> findByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);
}
