package com.ceos.beatbuddy.global.service;

import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import com.ceos.beatbuddy.global.util.UploadUtil;
import com.ceos.beatbuddy.global.util.UploadUtilAsyncWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageUploadService {

    private final UploadUtilAsyncWrapper uploadUtilAsyncWrapper;

    public List<String> uploadImagesParallel(List<MultipartFile> images, UploadUtil.BucketType type, String folder) {
        List<CompletableFuture<String>> futures = images.stream()
                .map(image -> uploadUtilAsyncWrapper.uploadAsync(image, type, folder))
                .toList();

        // 개별 업로드 결과를 처리하여 부분적 실패 허용
        return futures.stream()
                .map(future -> {
                    try {
                        return future.join();
                    } catch (Exception e) {
                        // 로깅 후 실패한 업로드는 예외 발생
                        log.error("이미지 업로드 실패", e);
                        throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
                    }
                })
                .toList();
    }
}
