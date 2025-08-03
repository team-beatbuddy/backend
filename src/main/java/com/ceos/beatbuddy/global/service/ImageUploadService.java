package com.ceos.beatbuddy.global.service;

import com.ceos.beatbuddy.global.util.UploadUtil;
import com.ceos.beatbuddy.global.util.UploadUtilAsyncWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final UploadUtilAsyncWrapper uploadUtilAsyncWrapper;

    public List<String> uploadImagesParallel(List<MultipartFile> images, UploadUtil.BucketType type, String folder) {
        List<CompletableFuture<String>> futures = images.stream()
                .map(image -> uploadUtilAsyncWrapper.uploadAsync(image, type, folder))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return futures.stream().map(CompletableFuture::join).toList();
    }
}
