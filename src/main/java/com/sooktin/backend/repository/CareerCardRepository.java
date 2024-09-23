package com.sooktin.backend.repository;

import com.sooktin.backend.domain.CareerCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CareerCardRepository extends JpaRepository<CareerCard, Long> {

    CareerCard save(CareerCard community);
    Optional<CareerCard> findByTitle(String title);
    Optional<CareerCard> findById(long id);
    List<CareerCard> findAll();

}
