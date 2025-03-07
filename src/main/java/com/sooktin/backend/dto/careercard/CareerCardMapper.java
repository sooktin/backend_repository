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
        //unknown property 있던 까닭 :>필드 이름 불일치 componentmodel로 빈 인식
    @Mapping(target = "userId", expression = "java(mapUserId(entity.getUser()))")
    @Mapping(target = "skills", expression = "java(mapSkills(entity.getSkills()))")
    @Mapping(target = "companiesInExperience", expression = "java(mapCompaniesInExperience(entity.getExperiences()))")
    @Mapping(target = "imageUrl", expression = "java(mapImageUrl(entity.getImageUrls()))")
    CareerCardDTO toDto(CareerCard entity);

    default List<CareerCardDTO> toDtoList(List<CareerCard> entities) {
        if (entities == null) return new ArrayList<>();
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    };

    default Long mapUserId(User user) {
        return user != null ? user.getId() : null;
    }

    default List<String> mapSkills(List<String> skills) {
        return skills != null && !skills.isEmpty() ? skills.subList(0, Math.min(skills.size(), 8)) : new ArrayList<>();
    }

    default List<String> mapCompaniesInExperience(List<Experience> experiences) {
        if (experiences == null || experiences.isEmpty()) return new ArrayList<>();
        return experiences.stream()
                .map(Experience::getCompany)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

    }

    default String mapImageUrl(List<String> imageUrls) {
        return imageUrls != null && !imageUrls.isEmpty() ? imageUrls.get(0) : "";
    }
}
