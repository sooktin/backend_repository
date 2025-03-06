package com.sooktin.backend.repository;

import com.sooktin.backend.domain.CareerCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CareerCardRepositoryCustom {
    //기본 검색
    Page<CareerCard> searchCareerCards(String keyword, Pageable pageable);
    //다중검색 or을 사용
    Page<CareerCard> searchCareerCardsWithOrCondition(String keyword,  Pageable pageable);
}
