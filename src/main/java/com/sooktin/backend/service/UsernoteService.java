package com.sooktin.backend.service;

import com.sooktin.backend.domain.Usernote;
import com.sooktin.backend.repository.UsernoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsernoteService {
    /**
     * 여기에서 비즈니스 로직을 만들어냅니다.
     * 스프링의 트랜잭션 관리를 받음
     **/
    @Autowired
    UsernoteRepository usernoteRepository;

    //이런 식으로!
    public Usernote saveNote(Usernote usernote) {
        return usernoteRepository.save(usernote);
    }
}
