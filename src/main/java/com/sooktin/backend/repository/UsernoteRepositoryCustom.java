package com.sooktin.backend.repository;

import com.sooktin.backend.domain.Usernote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface UsernoteRepositoryCustom {

    Page<Usernote> searchUsernotesWithOrCondition(String keyword, Pageable pageable);
}
