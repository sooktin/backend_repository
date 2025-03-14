package com.sooktin.backend.service;

import com.sooktin.backend.domain.Usernote;
import com.sooktin.backend.dto.usernote.FindMyUsernoteWithJWTResponse;
import com.sooktin.backend.dto.usernote.SearchUsernoteResponse;
import com.sooktin.backend.repository.UserRepository;
import com.sooktin.backend.repository.UsernoteRepository;
import com.sooktin.backend.repository.UsernoteRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UsernoteService {


    private final UsernoteRepository usernoteRepository;
    private final UsernoteRepositoryCustom usernoteRepositoryCustom;

    // C - Create post
    public Usernote createUsernote(Usernote usernote) {
        if (usernote.getContent().length() > 300) {
            throw new IllegalArgumentException("내용은 300자를 초과할 수 없습니다.");
        }
        return usernoteRepository.save(usernote);
    }

    // R - Read all posts
    public List<Usernote> findAll() {
        return usernoteRepository.findAll();
    }

    // R - Read post by ID
    @Cacheable(value = "userNote",key = "#id")
    public Optional<Usernote> findById(long id) {
        return usernoteRepository.findById(id);
    }

    // U - Update post by ID
    @CacheEvict(value = "userNote",allEntries = true)
    public Usernote updateUsernote(Long id, Usernote updatedUsernote) {
        Usernote usernote = usernoteRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 포스트가 존재하지 않습니다. id: " + id)
        );
        usernote.setContent(updatedUsernote.getContent());
        //usernote.setLikes(updatedUsernote.getLikes());
        return usernote;
    }

    // D - Delete post by ID
    @CacheEvict(value = "userNote",key = "#id")
    public boolean deleteById(long id) {
        if (usernoteRepository.existsById(id)) {
            usernoteRepository.deleteById(id);
            return true;
        } else {
            throw new IllegalArgumentException("해당 포스트가 존재하지 않습니다. id: " + id);
        }
    }
    @Cacheable(value = "userNote", key = "#email")
    public List<FindMyUsernoteWithJWTResponse> findByUserEmail(String email) {
        List<Usernote> usernotes = usernoteRepository.findByUser_Email(email);

        return usernotes.stream()
                .map(usernote -> {
                    FindMyUsernoteWithJWTResponse.FindMyUsernoteDto dto = new FindMyUsernoteWithJWTResponse.FindMyUsernoteDto();
                    dto.setId(usernote.getId());
                    //dto.setTitle(usernote.getTitle());
                    dto.setContent(usernote.getContent());
                    //dto.setLikes(usernote.getLikes());
                    dto.setCreatedAt(usernote.getCreated_at());
                    dto.setModifiedAt(usernote.getModified_at());
                    return new FindMyUsernoteWithJWTResponse(
                            200,
                            "내 유저노트 갖고 오기 성공",
                                    dto
                    );
                })
                .collect(Collectors.toList());

    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = "userNote",
            key = "#keyword",
            unless = "#result.usernotes.isEmpty()"
    )
    public SearchUsernoteResponse searchUsernotes(String keyword, Pageable pageable) {
        Page<Usernote> usernotes= usernoteRepositoryCustom.searchUsernotesWithOrCondition(keyword, pageable);
        return SearchUsernoteResponse.from(usernotes);

    }
}
