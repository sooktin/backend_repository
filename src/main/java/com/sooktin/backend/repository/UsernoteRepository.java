package com.sooktin.backend.repository;

import com.sooktin.backend.domain.Usernote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsernoteRepository extends JpaRepository<Usernote, Long> {

    Usernote save(Usernote usernote);
    Optional<Usernote> findByTitle(String title); // 제목으로 Usernote 엔터티 찾기
    Optional<Usernote> findById(long id);
    List<Usernote> findAll(); // 모든 Usernote 엔터티 리스트로 가져오기
    @Override
    void deleteById(Long id);  // ID로 Usernote 삭제
}
