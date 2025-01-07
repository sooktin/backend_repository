package com.sooktin.backend.service;

import com.sooktin.backend.domain.CareerCard;
import com.sooktin.backend.repository.CareerCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CareerCardService {

    private final CareerCardRepository careerCardRepository;

    @Autowired
    public CareerCardService(CareerCardRepository careerCardRepository) {
        this.careerCardRepository = careerCardRepository;
    }

    // C - Create CareerCard
    public CareerCard createCareerCard(CareerCard careerCard) {
        validateCareerCard(careerCard);

        if (careerCardRepository.findByUserId(careerCard.getUser().getId()).isPresent()) {
            throw new IllegalArgumentException("해당 유저는 이미 커리어카드를 가지고 있습니다.");
        }

        return careerCardRepository.save(careerCard);
    }

    // R - Read all CareerCards
    public List<CareerCard> findAll() {
        return careerCardRepository.findAll();
    }

    // R - Read CareerCard by CardId
    public Optional<CareerCard> findByCardId(Long cardId) {
        return careerCardRepository.findById(cardId);
    }

    // R - Read CareerCard by UserId
    public Optional<CareerCard> findByUserId(Long userId) {
        return careerCardRepository.findByUserId(userId);
    }

    // U - Update CareerCard by ID (본인 여부 확인 후 업데이트 진행)
    @Transactional
    public CareerCard updateCareerCard(Long cardId, CareerCard updatedCareerCard) {
        CareerCard existingCard = careerCardRepository.findById(cardId).orElseThrow(
                () -> new IllegalArgumentException("해당 커리어카드를 찾을 수 없습니다. id: " + cardId)
        );

        if (!isOwner(existingCard.getId())) {
            throw new IllegalStateException("본인이 아닌 사용자는 이 커리어카드를 수정할 수 없습니다.");
        }

        validateCareerCard(updatedCareerCard);
        updateCardFields(existingCard, updatedCareerCard);

        return careerCardRepository.save(existingCard);
    }



    // D - Delete CareerCard by ID
    public boolean deleteById(Long cardId) {
        if (careerCardRepository.existsById(cardId)) {
            careerCardRepository.deleteById(cardId);
            return true;
        } else {
            throw new IllegalArgumentException("해당 커리어카드가 존재하지 않습니다. id: " + cardId);
        }
    }

    // 검증 메소드
    public boolean isOwner(Long careerCardId) {
        // 현재 인증된 사용자의 정보를 가져옴
         // 로그인된 사용자 정보 (User 객체)

        // CareerCard를 조회하고 해당 카드의 소유자(userId)와 로그인된 사용자의 ID를 비교
/*        CareerCard careerCard = careerCardRepository.findById(careerCardId).orElseThrow(
                () -> new IllegalArgumentException("해당 커리어카드가 존재하지 않습니다. id: " + careerCardId)
        );*/

        // 본인 여부 검증
        // return careerCard.getUser().getId().equals(loggedInUser.getId());
        return true;
    }

    private void validateCareerCard(CareerCard careerCard) {
        // 닉네임 검증
        if (careerCard.getNickname() == null || careerCard.getNickname().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 비워둘 수 없습니다.");
        }

        // 재학 여부 검증
        validateStudentStatus(careerCard.getStudent_status());

        // 전공 검증
        validateMajor(careerCard.getMajor());

        // 이미지 리스트 크기 제한 (이미지가 비어 있을 수 있음)
        validateImageUrls(careerCard.getImageUrls());

        // 학번 검증
        if (careerCard.getStudent_num() == null || !careerCard.getStudent_num().matches("\\d{2}")) {
            throw new IllegalArgumentException("학번은 정확히 2자리 숫자여야 합니다.");
        }

        // 학년 검증
        if (careerCard.getGrade() < 1 || careerCard.getGrade() > 4) {
            throw new IllegalArgumentException("학년은 1~4 사이의 값이어야 합니다.");
        }

        // 경력 검증 (경력이 비어 있을 수 있음)
        validateExperiences(careerCard.getExperiences());

        // 기술 검증 (기술 정보가 비어 있을 수 있음)
        validateSkills(careerCard.getSkills());

        // 소속 검증 (소속 정보가 비어 있을 수 있음)
        validateDepartments(careerCard.getDepartments());
    }

    private void validateStudentStatus(String studentStatus) {
        if (!"재학".equals(studentStatus) && !"휴학".equals(studentStatus) && !"졸업".equals(studentStatus)) {
            throw new IllegalArgumentException("재학 여부는 '재학', '휴학', '졸업' 중 하나여야 합니다.");
        }
    }

    private void validateMajor(String major) {
        if (major == null || major.isEmpty()) {
            throw new IllegalArgumentException("전공은 비워둘 수 없습니다.");
        }
        if (major.length() > 10) {
            throw new IllegalArgumentException("전공 필드는 최대 10자까지 입력 가능합니다.");
        }
    }

    private void validateImageUrls(List<String> imageUrls) {
        if (imageUrls != null && imageUrls.size() > 3) {
            throw new IllegalArgumentException("이미지는 최대 3개까지 등록할 수 있습니다.");
        }
    }

    private void validateExperiences(List<String> experiences) {
        if (experiences != null) {
            for (String experience : experiences) {
                if (experience.length() > 1000) {
                    throw new IllegalArgumentException("각 경력 정보는 최대 1000자까지 입력 가능합니다.");
                }
            }
        }
    }

    private void validateSkills(List<String> skills) {
        if (skills != null) {
            for (String skill : skills) {
                if (skill.length() > 100) {
                    throw new IllegalArgumentException("각 기술 정보는 최대 100자까지 입력 가능합니다.");
                }
            }
        }
    }

    private void validateDepartments(List<String> departments) {
        if (departments != null && departments.size() > 5) {
            throw new IllegalArgumentException("소속 정보는 최대 5개까지 입력 가능합니다.");
        }
    }

    // Update existing CareerCard fields
    private void updateCardFields(CareerCard existingCard, CareerCard updatedCard) {
        existingCard.setNickname(updatedCard.getNickname());
        existingCard.setMajor(updatedCard.getMajor());
        existingCard.setStudent_status(updatedCard.getStudent_status());
        existingCard.setGrade(updatedCard.getGrade());
        existingCard.setStudent_num(updatedCard.getStudent_num());
        existingCard.setDepartments(updatedCard.getDepartments());
        existingCard.setExperiences(updatedCard.getExperiences());
        existingCard.setSkills(updatedCard.getSkills());
        existingCard.setImageUrls(updatedCard.getImageUrls());
    }

}
