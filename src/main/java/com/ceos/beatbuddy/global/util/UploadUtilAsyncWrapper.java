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

    @Async("uploadExecutor")
    public CompletableFuture<String> uploadAsync(MultipartFile image, UploadUtil.BucketType type, String folder) {
        String name = image.getOriginalFilename();
        long start = System.currentTimeMillis();
        log.info("▶ START Upload: {}", name);

        // 원본 업로드
        String originalUrl = uploadUtil.upload(image, type, folder);

        // post인 경우만 썸네일 생성
        if ("post".equals(folder)) {
            String originalFileName = extractFileNameFromUrl(originalUrl);
            uploadUtil.uploadThumbnail(image, type, folder, originalFileName);
        }

        long end = System.currentTimeMillis();
        log.info("✅ END Upload: {} ({} ms)", name, end - start);

        return CompletableFuture.completedFuture(originalUrl);
    }

    private String extractFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
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
