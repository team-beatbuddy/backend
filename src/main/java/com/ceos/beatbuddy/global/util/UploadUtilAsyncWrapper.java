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

        try {
            if ("post".equals(folder) || "review".equals(folder)) {
                // 순차 실행
                String originalUrl = uploadOriginalOnly(image, type, folder);
                String thumbnailUrl;

                if (UploadUtil.isVideoFile(image.getOriginalFilename())) {
                    // 비디오 → 썸네일 서비스
                    thumbnailUrl = videoThumbnailService.generateAndUploadThumbnail(image, folder);
                } else {
                    // 이미지 → 썸네일 생성
                    String fileName = generateFileName(image.getOriginalFilename());
                    thumbnailUrl = uploadUtil.uploadThumbnail(image, type, folder, fileName);
                }

                log.info("✅ Both uploads completed for: {}", name);

                UploadResult result = UploadResult.builder()
                        .originalUrl(originalUrl)
                        .thumbnailUrl(thumbnailUrl)
                        .build();

                long end = System.currentTimeMillis();
                log.info("✅ END Upload: {} ({} ms)", name, end - start);

                return CompletableFuture.completedFuture(result);

            } else {
                // post/review 아닌 경우 원본만
                String originalUrl = uploadUtil.upload(image, type, folder);

                UploadResult result = UploadResult.builder()
                        .originalUrl(originalUrl)
                        .thumbnailUrl(null)
                        .build();

                long end = System.currentTimeMillis();
                log.info("✅ END Upload: {} ({} ms)", name, end - start);

                return CompletableFuture.completedFuture(result);
            }
        } catch (Exception e) {
            log.error("Upload failed for {}: {}", name, e.getMessage());
            throw e;
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
