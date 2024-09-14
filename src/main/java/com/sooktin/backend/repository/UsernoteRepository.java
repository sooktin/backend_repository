package com.sooktin.backend.repository;

import com.sooktin.backend.domain.Usernote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsernoteRepository extends JpaRepository<Usernote, Long> {
    // 제목으로 Usernote 엔터티 찾기
    Optional<Usernote> findByTitle(String title);

    // ID로 Usernote 엔터티 찾기
    Optional<Usernote> findById(Long id);

    // 모든 Usernote 엔터티 리스트로 가져오기
    List<Usernote> findAll();

    // ID로 Usernote 삭제
    @Override
    void deleteById(Long id);
}
