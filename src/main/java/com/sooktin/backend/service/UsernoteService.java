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

    private final UsernoteRepository usernoteRepository;

    @Autowired
    public UsernoteService(UsernoteRepository usernoteRepository) {
        this.usernoteRepository = usernoteRepository;
    }

    // C - Create post
    public Usernote createUsernote(Usernote usernote) {
        if (usernote.getContent().length() > 300) {
            throw new IllegalArgumentException("내용은 300자를 초과할 수 없습니다.");
        }
        if (usernote.getTitle() == null || usernote.getTitle().isEmpty()) {
            throw new IllegalArgumentException("제목은 비워둘 수 없습니다.");
        }
        return usernoteRepository.save(usernote);
    }

    // R - Read all posts
    public List<Usernote> findAll() {
        return usernoteRepository.findAll();
    }

    // R - Read post by ID
    public Optional<Usernote> findById(long id) {
        return usernoteRepository.findById(id);
    }

    // U - Update post by ID
    @Transactional
    public Usernote updateUsernote(Long id, Usernote updatedUsernote) {
        Usernote usernote = usernoteRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 포스트가 존재하지 않습니다. id: " + id)
        );
        usernote.setTitle(updatedUsernote.getTitle());
        usernote.setContent(updatedUsernote.getContent());
        usernote.setLikes(updatedUsernote.getLikes());
        return usernote;
    }

    // D - Delete post by ID
    public boolean deleteById(long id) {
        if (usernoteRepository.existsById(id)) {
            usernoteRepository.deleteById(id);
            return true;
        } else {
            throw new IllegalArgumentException("해당 포스트가 존재하지 않습니다. id: " + id);
        }
    }
}
