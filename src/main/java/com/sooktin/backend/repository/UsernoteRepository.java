package com.sooktin.backend.repository;

import com.sooktin.backend.domain.Usernote;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsernoteRepository extends JpaRepository<Usernote, Long> {
    /**
     *  domain의 usernote 엔터티 햇죠?
     *  그 엔터티를 갖고와서 findAll,findById,deleteById 등을 수행합니다.
     *   DAO = Repository
     *   왜 인터페이스인지 찾아보세요!
     **/

}
