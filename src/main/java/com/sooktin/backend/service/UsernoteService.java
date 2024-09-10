package com.sooktin.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sooktin.backend.domain.Usernote;
import com.sooktin.backend.repository.UsernoteRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UsernoteService {
    /**
     * 여기에서 게시판 CRUD 의 비즈니스 로직을 만들어냅니다.
     * 스프링의 트랜잭션 관리를 받음
     **/
    private final  UsernoteRepository UsernoteRepository;

    @Autowired
    public  UsernoteService(UsernoteRepository UsernoteRepository) {
        this.UsernoteRepository = UsernoteRepository;
    }

    //C - create post
    public Usernote save(Usernote Usernote) {
        return UsernoteRepository.save(Usernote);
    }
    //R - read all post
    public List<Usernote> findAll() {
        return UsernoteRepository.findAll();
    }
    //R - read post by id
    public Optional<Usernote> findById(long id) {
        return UsernoteRepository.findById(id);
    }

    /*

    // U - update by id
    public Long updateUsernote(Usernote Usernote) {
        Usernote Usernote = UsernoteRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 아이디가 존재하지 않습니다. ")
        );
        Usernote update();
        return ;
    }
    //입력값을 받아와야 그 입력값으로 하는 건데 입력값 구현 X


    /*
    *  // 글 수정
    @Transactional
    public Long updateBoard(Long id, BoardRequestDto requestDto) {
        Board board = boardRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 아이디가 존재하지 않습니다.")
        );
        board.update(requestDto);
        return board.getId();
    }
    * */
    /*
    //D - delete post by id
    public void deleteById(long id) {
        UsernoteRepository.deleteById(id);
    }
    */
}
