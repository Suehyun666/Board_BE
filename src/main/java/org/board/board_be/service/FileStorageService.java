package org.board.board_be.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    // application.yml에 file.upload-dir 설정을 추가해야 합니다.
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    public String store(MultipartFile file) {
        try {
            // 업로드 디렉토리가 없으면 생성
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 원본 파일명 정제 (경로 조작 방지)
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

            // 저장할 파일명 생성 (UUID + 확장자)
            String extension = "";
            int i = originalFileName.lastIndexOf('.');
            if (i > 0) {
                extension = originalFileName.substring(i);
            }
            String storedFileName = UUID.randomUUID().toString() + extension;

            // 파일 저장
            Path targetLocation = uploadPath.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 접근 가능한 URL 반환 (예: /uploads/uuid.jpg)
            // WebConfig에서 /uploads/** 경로를 실제 폴더와 매핑해줘야 함
            return "/uploads/" + storedFileName;

        } catch (IOException ex) {
            throw new RuntimeException("파일을 저장할 수 없습니다. 다시 시도해주세요.", ex);
        }
    }

    /**
     * 파일 다운로드를 위한 Resource 반환
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(fileName).normalize();

            // 경로 조작 공격 방지: uploadPath 외부 파일 접근 차단
            if (!filePath.startsWith(uploadPath)) {
                throw new RuntimeException("잘못된 파일 경로입니다.");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("파일을 찾을 수 없습니다: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("파일을 찾을 수 없습니다: " + fileName, ex);
        }
    }
}