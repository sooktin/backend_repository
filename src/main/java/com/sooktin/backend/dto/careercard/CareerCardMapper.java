package com.sooktin.backend.dto.careercard;

import com.sooktin.backend.domain.CareerCard;
import com.sooktin.backend.domain.Experience;
import com.sooktin.backend.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CareerCardMapper {
    CareerCardMapper INSTANCE = Mappers.getMapper(CareerCardMapper.class);

    @Mapping(target = "cardId", source = "id")
    @Mapping(target = "nickname", source = "user.nickname")
    @Mapping(target = "major", source = "major")
    @Mapping(target = "student_num", source = "student_num")
    @Mapping(target = "student_status", source = "student_status")
    @Mapping(target = "grade", source = "grade")
    @Mapping(target = "job", source = "job")
    @Mapping(target = "department", source = "department")
    @Mapping(target = "experiences", expression = "java(mapExperiences(entity.getExperiences()))")
    @Mapping(target = "skills", source = "skills")
    @Mapping(target = "imageUrls", source = "imageUrls")
    CareerCardDTO toDto(CareerCard entity);

    default List<CareerCardDTO.ExperienceDTO> mapExperiences(List<Experience> experiences) {
        return experiences.stream()
                .map(exp -> CareerCardDTO.ExperienceDTO.builder()
                        .company(exp.getCompany())
                        .period(exp.getPeriod())
                        .build())
                .collect(Collectors.toList());
    }

    default List<CareerCardDTO> toDtoList(List<CareerCard> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
