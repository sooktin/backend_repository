package com.sooktin.backend.dto.usernote;

import com.sooktin.backend.domain.Usernote;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchUsernoteResponse {
    private List<UsernoteDTO> usernotes;
    private int totalCount;
    private int page;
    private int size;
    private int totalPages;

    public static SearchUsernoteResponse from(Page<Usernote> page) {
        List<UsernoteDTO> usernoteDTOs = page.getContent().stream()
                .map(usernote -> new UsernoteDTO(
                        usernote.getId(),
                        usernote.getContent(),
                        usernote.getUser().getId() // ✅ User 대신 userId 사용
                ))
                .collect(Collectors.toList());

        return new SearchUsernoteResponse(
                usernoteDTOs,
                (int) page.getTotalElements(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages()
        );
    }
}