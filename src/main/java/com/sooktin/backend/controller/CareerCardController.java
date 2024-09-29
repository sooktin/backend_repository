package com.sooktin.backend.controller;

import com.sooktin.backend.domain.CareerCard;
import com.sooktin.backend.service.CareerCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/career-card")
public class CareerCardController {

    private final CareerCardService careerCardService;

    @Autowired
    public CareerCardController(CareerCardService careerCardService) {
        this.careerCardService = careerCardService;
    }

    // 모든 CareerCard 목록 조회
    @GetMapping("/all")
    public ResponseEntity<?> getAllCareerCards() {
        try {
            List<CareerCard> careerCards = careerCardService.findAll();
            return ResponseEntity.ok(careerCards);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("커리어카드 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 특정 CareerCard 조회
    @GetMapping("/{id}")
    public ResponseEntity<?> getCareerCardById(@PathVariable Long id) {
        try {
            Optional<CareerCard> careerCard = careerCardService.findById(id);
            return careerCard.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build()); // 데이터가 없으면 404 반환
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("커리어카드를 조회하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // CareerCard 생성
    @PostMapping("/")
    public ResponseEntity<?> createCareerCard(@RequestBody CareerCard careerCard) {
        try {
            CareerCard createdCareerCard = careerCardService.createCareerCard(careerCard);
            return ResponseEntity.ok(createdCareerCard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("커리어카드를 생성하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

 /*   // CareerCard 수정
    @PatchMapping("/edit")
    public ResponseEntity<?> updateCareerCard(@RequestBody CareerCard careerCardDetails) {
        try {
            // 1. 현재 로그인된 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User loggedInUser = (User) authentication.getPrincipal(); // 현재 로그인된 사용자

            // 2. 로그인된 사용자의 userId에 해당하는 CareerCard를 찾기
            CareerCard userCareerCard = careerCardService.findByUserId(loggedInUser.getId());

            if (userCareerCard == null) {
                return ResponseEntity.notFound().build(); // 해당 커리어카드가 없을 경우 404 반환
            }

            // 3. CareerCard 업데이트 진행 (입력받은 정보로 업데이트)
            CareerCard updatedCareerCard = careerCardService.updateCareerCard(userCareerCard.getId(), careerCardDetails);

            // 4. 업데이트된 CareerCard 반환
            return ResponseEntity.ok(updatedCareerCard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("커리어카드를 수정하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }*/

    @PatchMapping("/edit")
    public ResponseEntity<?> updateCareerCard(@PathVariable Long id, @RequestBody CareerCard careerCardDetails) {
        try {
            CareerCard updatedCareerCard = careerCardService.updateCareerCard(id, careerCardDetails);
            return ResponseEntity.ok(updatedCareerCard);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body("해당 커리어카드를 수정할 권한이 없습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("커리어카드를 수정하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    // /career-card/edit 엔드포인트: 현재 로그인된 사용자의 CareerCard 수정

    // CareerCard 삭제
    @DeleteMapping("/careercard/{id}")
    public ResponseEntity<?> deleteCareerCard(@PathVariable Long id) {
        try {
            if (careerCardService.deleteById(id)) { // 삭제 성공
                return ResponseEntity.noContent().build(); // 204 반환
            } else {
                return ResponseEntity.badRequest().body("해당 커리어카드를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("커리어카드를 삭제하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
