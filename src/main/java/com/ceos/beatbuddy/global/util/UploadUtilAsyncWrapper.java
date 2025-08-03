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

        String url = uploadUtil.upload(image, type, folder);

        long end = System.currentTimeMillis();
        log.info("✅ END Upload: {} ({} ms)", name, end - start);

        return CompletableFuture.completedFuture(url);
    }


    @Async("uploadExecutor")
    public void deleteImageAsync(String imageUrl, UploadUtil.BucketType type) {
        uploadUtil.deleteImage(imageUrl, type);
    }

    @Async("uploadExecutor")
    public void deleteImagesAsync(List<String> imageUrls, UploadUtil.BucketType type) {
        uploadUtil.deleteImages(imageUrls, type);
    }
}
