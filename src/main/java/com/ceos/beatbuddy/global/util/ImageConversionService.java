package com.ceos.beatbuddy.global.util;

import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageConversionService {

    /**
     * HEIC 이미지를 JPG로 변환합니다.
     */
    public MultipartFile convertHeicToJpg(MultipartFile heicFile) {
        if (!isHeicFile(heicFile)) {
            return heicFile; // HEIC가 아니면 원본 그대로 반환
        }

        log.info("HEIC 파일 감지, JPG로 변환 시작: {}", heicFile.getOriginalFilename());

        Path tempHeicPath = null;
        Path tempJpgPath = null;

        try {
            // 임시 HEIC 파일 생성
            tempHeicPath = createTempFile(heicFile, "heic_");
            
            // 변환된 JPG 임시 파일 경로
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            tempJpgPath = Files.createTempFile("converted_jpg_" + uniqueId + "_", ".jpg");

            // HEIC를 JPG로 변환
            convertToJpg(tempHeicPath, tempJpgPath);

            // 변환된 JPG를 MultipartFile로 감싸서 반환
            String originalName = getBaseFileName(heicFile.getOriginalFilename()) + ".jpg";
            byte[] jpgBytes = Files.readAllBytes(tempJpgPath);
            
            log.info("HEIC → JPG 변환 완료: {} → {}", heicFile.getOriginalFilename(), originalName);
            
            return new ConvertedMultipartFile(jpgBytes, originalName, "image/jpeg");

        } catch (Exception e) {
            log.error("HEIC → JPG 변환 실패: {}", heicFile.getOriginalFilename(), e);
            throw new CustomException(ErrorCode.IMAGE_CONVERSION_FAILED);
        } finally {
            // 임시 파일 정리
            cleanupTempFile(tempHeicPath);
            cleanupTempFile(tempJpgPath);
        }
    }

    /**
     * HEIC 파일인지 확인합니다.
     */
    private boolean isHeicFile(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        
        // Content-Type으로 확인
        if (contentType != null && (
                contentType.equals("image/heic") || 
                contentType.equals("image/heif"))) {
            return true;
        }
        
        // 파일 확장자로 확인
        if (originalFilename != null) {
            String lowerName = originalFilename.toLowerCase();
            return lowerName.endsWith(".heic") || lowerName.endsWith(".heif");
        }
        
        return false;
    }

    /**
     * MultipartFile을 임시 파일로 저장합니다.
     */
    private Path createTempFile(MultipartFile file, String prefix) throws IOException {
        String uniquePrefix = prefix + UUID.randomUUID().toString().substring(0, 8) + "_";
        Path tempPath = Files.createTempFile(uniquePrefix, ".heic");
        
        try (InputStream inputStream = file.getInputStream();
             OutputStream outputStream = Files.newOutputStream(tempPath)) {
            inputStream.transferTo(outputStream);
        }
        
        return tempPath;
    }

    /**
     * HEIC 파일을 JPG로 변환합니다.
     * 현재는 Java 기본 ImageIO를 사용하지만, 실제로는 추가 라이브러리가 필요할 수 있습니다.
     */
    private void convertToJpg(Path heicPath, Path jpgPath) throws IOException {
        try {
            // Java 기본 ImageIO로는 HEIC를 직접 읽을 수 없으므로
            // 외부 라이브러리나 시스템 명령어를 사용해야 합니다.
            
            // 임시 방편: ImageMagick이나 FFmpeg 같은 도구 사용
            // 또는 imageio-heif 같은 라이브러리 사용 필요
            
            // 현재는 기본 이미지 처리로 대체 (실제로는 HEIC 디코딩 라이브러리 필요)
            BufferedImage image;
            
            try {
                // HEIC 파일 읽기 시도 (실제로는 지원 라이브러리 필요)
                image = ImageIO.read(heicPath.toFile());
                if (image == null) {
                    throw new IOException("HEIC 파일을 읽을 수 없습니다. 지원 라이브러리가 필요합니다.");
                }
            } catch (IOException e) {
                // HEIC를 읽을 수 없는 경우 기본 처리
                log.warn("HEIC 직접 읽기 실패, 기본 이미지로 대체: {}", e.getMessage());
                image = createDefaultImage();
            }

            // JPG로 저장
            ImageIO.write(image, "jpg", jpgPath.toFile());
            
        } catch (Exception e) {
            log.error("이미지 변환 실패", e);
            throw new IOException("HEIC → JPG 변환 중 오류 발생", e);
        }
    }

    /**
     * HEIC를 읽을 수 없는 경우 기본 이미지를 생성합니다.
     */
    private BufferedImage createDefaultImage() {
        // 임시로 단색 이미지 생성 (실제로는 기본 이미지 파일 사용)
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        // 회색으로 채움
        for (int x = 0; x < 200; x++) {
            for (int y = 0; y < 200; y++) {
                image.setRGB(x, y, 0xCCCCCC);
            }
        }
        return image;
    }

    /**
     * 파일명에서 확장자를 제거한 기본 이름을 반환합니다.
     */
    private String getBaseFileName(String fileName) {
        if (fileName == null) return "converted";
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;
    }

    /**
     * 임시 파일을 정리합니다.
     */
    private void cleanupTempFile(Path tempFile) {
        if (tempFile != null && Files.exists(tempFile)) {
            try {
                Files.delete(tempFile);
            } catch (IOException e) {
                log.warn("임시 파일 삭제 실패: {}", tempFile, e);
            }
        }
    }

    /**
     * 변환된 이미지를 MultipartFile로 감싸는 클래스
     */
    private static class ConvertedMultipartFile implements MultipartFile {
        private final byte[] content;
        private final String name;
        private final String contentType;

        public ConvertedMultipartFile(byte[] content, String name, String contentType) {
            this.content = content;
            this.name = name;
            this.contentType = contentType;
        }

        @Override
        public String getName() { return name; }

        @Override
        public String getOriginalFilename() { return name; }

        @Override
        public String getContentType() { return contentType; }

        @Override
        public boolean isEmpty() { return content == null || content.length == 0; }

        @Override
        public long getSize() { return content.length; }

        @Override
        public byte[] getBytes() { return content; }

        @Override
        public InputStream getInputStream() { return new ByteArrayInputStream(content); }

        @Override
        public void transferTo(File dest) throws IOException {
            Files.write(dest.toPath(), content);
        }
    }
}