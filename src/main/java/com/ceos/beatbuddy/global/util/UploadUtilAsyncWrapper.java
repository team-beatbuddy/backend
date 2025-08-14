package com.ceos.beatbuddy.global.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class UploadUtilAsyncWrapper {

    private final UploadUtil uploadUtil;
    private final VideoThumbnailService videoThumbnailService;

    @Async("uploadExecutor")
    public CompletableFuture<UploadResult> uploadAsync(MultipartFile image, UploadUtil.BucketType type, String folder) {
        String name = image.getOriginalFilename();
        long start = System.currentTimeMillis();
        log.info("▶ START Upload: {}", name);

        if ("post".equals(folder) || "review".equals(folder)) {
            // post/review 폴더인 경우: 원본과 썸네일을 진짜 병렬로 업로드
            CompletableFuture<String> originalFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    // 비디오 썸네일 생성을 제외한 원본 업로드만 수행
                    return uploadOriginalOnly(image, type, folder);
                } catch (Exception e) {
                    log.error("Failed to upload original for {}: {}", name, e.getMessage());
                    throw new RuntimeException("Original upload failed", e);
                }
            });
            
            CompletableFuture<String> thumbnailFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    // 비디오 파일인지 확인
                    if (UploadUtil.isVideoFile(image.getOriginalFilename())) {
                        // 비디오 파일인 경우 VideoThumbnailService 사용
                        return videoThumbnailService.generateAndUploadThumbnail(image, folder);
                    } else {
                        // 이미지 파일인 경우 기존 uploadThumbnail 사용
                        String fileName = generateFileName(image.getOriginalFilename());
                        return uploadUtil.uploadThumbnail(image, type, folder, fileName);
                    }
                } catch (Exception e) {
                    log.error("Failed to upload thumbnail for {}: {}", name, e.getMessage());
                    throw new RuntimeException("Thumbnail upload failed", e);
                }
            });
            
            // 두 작업 모두 완료 대기
            String originalUrl = originalFuture.join();
            String thumbnailUrl = thumbnailFuture.join();
            
            log.info("✅ Both uploads completed for: {}", name);
            
            UploadResult result = UploadResult.builder()
                    .originalUrl(originalUrl)
                    .thumbnailUrl(thumbnailUrl)
                    .build();
            
            long end = System.currentTimeMillis();
            log.info("✅ END Upload: {} ({} ms)", name, end - start);
            
            return CompletableFuture.completedFuture(result);
        } else {
            // post/review가 아닌 경우: 원본만 업로드
            String originalUrl = uploadUtil.upload(image, type, folder);
            
            UploadResult result = UploadResult.builder()
                    .originalUrl(originalUrl)
                    .thumbnailUrl(null)
                    .build();
            
            long end = System.currentTimeMillis();
            log.info("✅ END Upload: {} ({} ms)", name, end - start);
            
            return CompletableFuture.completedFuture(result);
        }
    }

    private String extractFileNameFromUrl(String url) {
        return FileNameUtil.extractFileNameFromUrl(url);
    }
    
    private String uploadOriginalOnly(MultipartFile image, UploadUtil.BucketType type, String folder) {
        // UploadUtil의 uploadOriginalOnly 메소드 사용
        return uploadUtil.uploadOriginalOnly(image, type, folder);
    }
    
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
               "_" + java.util.UUID.randomUUID().toString() + extension;
    }

    @Async("uploadExecutor")
    public void deleteImageAsync(String imageUrl, UploadUtil.BucketType type) {
        uploadUtil.deleteImage(imageUrl, type);

        // post의 경우 썸네일도 함께 삭제
        if (imageUrl.contains("/post/")) {
            String thumbnailUrl = imageUrl.replace("/post/", "/post/thumbnail/");
            uploadUtil.deleteImage(thumbnailUrl, type);
        }
    }

    @Async("uploadExecutor")
    public void deleteImagesAsync(List<String> imageUrls, UploadUtil.BucketType type) {
        uploadUtil.deleteImages(imageUrls, type);
        // post의 경우 썸네일도 함께 삭제
        for (String imageUrl : imageUrls) {
            if (imageUrl.contains("/post/")) {
                String thumbnailUrl = imageUrl.replace("/post/", "/post/thumbnail/");
                uploadUtil.deleteImage(thumbnailUrl, type);
            }
        }
    }
}
