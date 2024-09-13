package com.sooktin.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sooktin.backend.domain.Usernote;
import com.sooktin.backend.repository.UsernoteRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsernoteService {
    /**
     * 여기에서 게시판 CRUD 의 비즈니스 로직을 만들어냅니다.
     * 스프링의 트랜잭션 관리를 받음
     **/
    private final  UsernoteRepository usernoteRepository;

    @Autowired
    public  UsernoteService(UsernoteRepository usernoteRepository) {
        this.usernoteRepository = usernoteRepository;
    }

    //C - create post
    public Usernote createUsernote(Usernote usernote) {
        // 검증 메소드 필요
        if (usernote.getContent().length() > 300) {
            throw new IllegalArgumentException("내용은 300자를 초과할 수 없습니다.");
        }
        if (usernote.getTitle() == null || usernote.getTitle().isEmpty()) {
            throw new IllegalArgumentException("제목은 비워둘 수 없습니다.");
        }
        return usernoteRepository.save(usernote);
    }

    //R - read all post
    public List<Usernote> findAll() {
        return usernoteRepository.findAll();
    }

    //R - read post by id
    public Optional<Usernote> findById(long id) {
        return usernoteRepository.findById(id);
    }


    // U - update by id
    @Transactional
    public Usernote updateUsernote(Long id, Usernote updatedUsernote) {
        Usernote usernote = usernoteRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 포스트가 존재하지 않습니다. id: " + id)
        );

        // 업데이트할 필드 설정
        usernote.setTitle(updatedUsernote.getTitle());
        usernote.setContent(updatedUsernote.getContent());
        usernote.setLikes(updatedUsernote.getLikes());

        return usernote;
    }

    //D - delete post by id
    public boolean deleteById(long id) {
        if (usernoteRepository.existsById(id)) {
            usernoteRepository.deleteById(id);
            return true;
        } else {
            throw new IllegalArgumentException("해당 포스트가 존재하지 않습니다. id: " + id);
        }
    }

}
