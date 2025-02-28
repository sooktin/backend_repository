package com.sooktin.backend.dto.careercard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor

public class SearchCareerCardResponse  {
    private  List<CareerCardDTO> careerCards;
    private  int totalCount;
    private  int page;
    private  int size;
}
