package com.sooktin.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sooktin.backend.domain.Community;
import com.sooktin.backend.repository.CommunityRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CommunityService {
    /**
     * 여기에서 게시판 CRUD 의 비즈니스 로직을 만들어냅니다.
     * 스프링의 트랜잭션 관리를 받음
     **/
    private final  CommunityRepository communityRepository;

    @Autowired
    public  CommunityService(CommunityRepository communityRepository) {
        this.communityRepository = communityRepository;
    }

    //C - create post
    public Community save(Community community) {
        return communityRepository.save(community);
    }
    //R - read all post
    public List<Community> findAll() {
        return communityRepository.findAll();
    }
    //R - read post by id
    public Optional<Community> findById(long id) {
        return communityRepository.findById(id);
    }

    // U - update by id
    public Long updateCommunity(Community community) {
        Community community = communityRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 아이디가 존재하지 않습니다. ")
        );
        community update();
        return
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

    //D - delete post by id
    public void deleteById(long id) {
        communityRepository.deleteById(id);
    }

}
