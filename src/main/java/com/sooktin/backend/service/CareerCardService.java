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
        return careerCardRepository.save(careerCard);
    }

    // R - Read all CareerCards
    public List<CareerCard> findAll() {
        return careerCardRepository.findAll();
    }

    // R - Read CareerCard by ID
    public Optional<CareerCard> findById(Long id) {
        return careerCardRepository.findById(id);
    }

    // U - Update CareerCard by ID (본인 여부 확인 후 업데이트 진행)
    @Transactional
    public CareerCard updateCareerCard(Long id, CareerCard updatedCareerCard) {
        // 본인 여부 확인
        if (!isOwner(id)) {
            throw new IllegalStateException("본인이 아닌 사용자는 이 커리어카드를 수정할 수 없습니다.");
        }

        // 본인이 맞으면 업데이트 진행
        CareerCard careerCard = careerCardRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 커리어카드가 존재하지 않습니다. id: " + id)
        );

        careerCard.setNickname(updatedCareerCard.getNickname()); // 닉네임 수정
        careerCard.setMajor(updatedCareerCard.getMajor()); // 전공 수정
        careerCard.setStudent_status(updatedCareerCard.isStudent_status()); // 재학 여부 수정
        careerCard.setGrade(updatedCareerCard.getGrade()); // 학년 수정
        careerCard.setDepartment(updatedCareerCard.getDepartment()); // 소속 수정
        careerCard.setExperience(updatedCareerCard.getExperience()); // 경력 수정
        careerCard.setSkills(updatedCareerCard.getSkills()); // 기술 수정
        careerCard.setImage_url(updatedCareerCard.getImage_url()); // 이미지 수정

        return careerCardRepository.save(careerCard); // 수정일시 자동 업데이트됨!
    }


    // D - Delete CareerCard by ID
    public boolean deleteById(Long id) {
        if (careerCardRepository.existsById(id)) {
            careerCardRepository.deleteById(id);
            return true;
        } else {
            throw new IllegalArgumentException("해당 커리어카드가 존재하지 않습니다. id: " + id);
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

    public void validateCareerCard(CareerCard careerCard) {
        // 1. 닉네임 검증
        if (careerCard.getNickname() == null || careerCard.getNickname().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 비워둘 수 없습니다.");
        }

        // 2. 재학 여부 검증
        if (!careerCard.isStudent_status()) {
            throw new IllegalArgumentException("재학 여부는 필수 항목입니다.");
        }

        // 3. 전공(major) 필드 검증
        if (careerCard.getMajor() == null || careerCard.getMajor().isEmpty()) {
            throw new IllegalArgumentException("전공은 비워둘 수 없습니다.");
        }
        if (careerCard.getMajor().length() > 10) {
            throw new IllegalArgumentException("전공 필드는 최대 10자까지 입력 가능합니다.");
        }

        // 4. 이미지 URL 검증 (유효한 URL 형식인지)
        if (careerCard.getImage_url() != null && !careerCard.getImage_url().isEmpty()) {
                throw new IllegalArgumentException("유효하지 않은 URL 형식입니다.");
        }

        // 5. 학번(student_num) 필드 검증
        if (careerCard.getStudent_num() == null || !careerCard.getStudent_num().matches("\\d{2}")) {
            throw new IllegalArgumentException("학번은 정확히 2자리 숫자여야 합니다.");
        }

        // 6. 학년(grade) 필드 검증
        if (careerCard.getGrade() < 1 || careerCard.getGrade() > 4) {
            throw new IllegalArgumentException("학년은 1~4 사이의 값이어야 합니다.");
        }

        // 7. 경력(experience) 필드 검증 (길이 제한)
        if (careerCard.getExperience() != null && careerCard.getExperience().length() > 5000) {
            throw new IllegalArgumentException("경력 정보는 최대 5000자까지 입력 가능합니다.");
        }

        // 8. 기술(skills) 필드 검증
        if (careerCard.getSkills() == null || careerCard.getSkills().isEmpty()) {
            throw new IllegalArgumentException("기술 정보는 비워둘 수 없습니다.");
        }

        // 9. 소속(department) 필드 검증
        if (careerCard.getDepartment() != null && careerCard.getDepartment().length() > 20) {
            throw new IllegalArgumentException("소속 필드는 최대 20자까지 입력 가능합니다.");
        }
    }

}
