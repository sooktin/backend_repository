package com.sooktin.backend.dto.usernote;

import com.sooktin.backend.domain.Usernote;
import lombok.Data;

import java.util.List;

@Data
public class SearchUsernoteResponse {
    //TODO UserNoteDTO로 바꾸시오!
    private List<Usernote> usernotes;
    private int totalCount;
    private int page;
    private int size;
}
