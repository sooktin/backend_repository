package com.sooktin.backend.repository;

import com.sooktin.backend.domain.CareerCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CareerCardRepository extends JpaRepository<CareerCard, Long>, CareerCardRepositoryCustom {

    CareerCard save(CareerCard community);
    Optional<CareerCard> findById(long id);
    List<CareerCard> findAll();
    Optional<CareerCard> findByUserId(Long userId);


    Page<CareerCard> searchCareerCards(String keyword, Pageable pageable);
}
