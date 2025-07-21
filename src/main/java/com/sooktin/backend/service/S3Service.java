package com.sooktin.backend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3-bucket}")
    private String bucketName;

    // 단일 이미지 업로드
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty() || Objects.isNull(file.getOriginalFilename())) {
            return null; // 예외 발생 대신 null 반환
        }

        String fileName = "images/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata));

            return amazonS3.getUrl(bucketName, fileName).toString(); // 업로드된 파일의 URL 반환
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류 발생", e);
        }
    }

    public void deleteImage(String imageUrl) {
        String key = extractKeyFromUrl(imageUrl);
        if (amazonS3.doesObjectExist(bucketName, key)) {
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
        } else {
            throw new IllegalArgumentException("삭제할 파일이 존재하지 않습니다: " + imageUrl);
        }
    }
    // 전체 이미지 삭제 (DB에서 URL 목록 가져와 삭제)
    public void deleteAllImages(List<String> imageUrls) {
        imageUrls.forEach(this::deleteImage);
    }

    // 단일 이미지 조회 (S3의 URL 반환)
    public String getImageUrl(String imageUrl) {
        return amazonS3.getUrl(bucketName, extractKeyFromUrl(imageUrl)).toString();
    }

    // 전체 이미지 조회 (DB에서 이미지 URL 가져오는 방식)
    public List<String> getAllImageUrls(List<String> imageUrls) {
        return imageUrls.stream()
                .map(this::getImageUrl)
                .collect(Collectors.toList());
    }

    // URL에서 S3 키 추출 (삭제 시 사용)
    private String extractKeyFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new IllegalArgumentException("이미지 URL이 비어 있습니다.");
        }

        try {
            URL url = new URL(imageUrl);
            String key = URLDecoder.decode(url.getPath(), "UTF-8");
            return key.startsWith("/") ? key.substring(1) : key;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("잘못된 URL 형식입니다: " + imageUrl);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URL 디코딩 중 오류 발생", e);
        }
    }
}
