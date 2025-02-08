package com.sooktin.backend.controller;

import com.sooktin.backend.dto.ResponseDto;
import com.sooktin.backend.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    // 단일 이미지 업로드
    @PostMapping("/upload")
    public ResponseEntity<ResponseDto<String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = s3Service.uploadImage(file);
        return ResponseEntity.ok(new ResponseDto<>(200, "이미지 업로드 성공", imageUrl));
    }

    // 단일 이미지 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<ResponseDto<Void>> deleteImage(@RequestParam String imageUrl) {
        s3Service.deleteImage(imageUrl);
        return ResponseEntity.ok(new ResponseDto<>(200, "이미지 삭제 성공", null));
    }

    // 전체 이미지 삭제
    @DeleteMapping("/delete-all")
    public ResponseEntity<ResponseDto<Void>> deleteAllImages(@RequestBody List<String> imageUrls) {
        s3Service.deleteAllImages(imageUrls);
        return ResponseEntity.ok(new ResponseDto<>(200, "모든 이미지 삭제 성공", null));
    }

    // 단일 이미지 조회
    @GetMapping("/image")
    public ResponseEntity<ResponseDto<String>> getImage(@RequestParam String imageUrl) {
        String url = s3Service.getImageUrl(imageUrl);
        return ResponseEntity.ok(new ResponseDto<>(200, "이미지 조회 성공", url));
    }

    // 전체 이미지 조회 (DB에서 리스트 받아오기)
    @GetMapping("/images")
    public ResponseEntity<ResponseDto<List<String>>> getAllImages(@RequestBody List<String> imageUrls) {
        List<String> urls = s3Service.getAllImageUrls(imageUrls);
        return ResponseEntity.ok(new ResponseDto<>(200, "전체 이미지 조회 성공", urls));
    }
}