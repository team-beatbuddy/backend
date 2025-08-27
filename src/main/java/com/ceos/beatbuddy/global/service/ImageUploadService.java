package com.ceos.beatbuddy.global.service;

import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import com.ceos.beatbuddy.global.util.UploadResult;
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
        List<CompletableFuture<UploadResult>> futures = images.stream()
                .map(image -> uploadUtilAsyncWrapper.uploadAsync(image, type, folder))
                .toList();

        // 모든 업로드가 완료될 때까지 병렬 대기 (성능 최적화)
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        
        try {
            allOf.join(); // 모든 업로드 완료 대기
            
            // 완료된 결과들을 수집
            return futures.stream()
                    .map(future -> {
                        try {
                            return future.join().getOriginalUrl();
                        } catch (Exception e) {
                            log.error("이미지 업로드 실패", e);
                            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
                        }
                    })
                    .toList();
        } catch (Exception e) {
            log.error("병렬 이미지 업로드 중 오류 발생", e);
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }
    
    public List<UploadResult> uploadImagesWithThumbnails(List<MultipartFile> images, UploadUtil.BucketType type, String folder) {
        List<CompletableFuture<UploadResult>> futures = images.stream()
                .map(image -> uploadUtilAsyncWrapper.uploadAsync(image, type, folder))
                .toList();

        // 모든 업로드가 완료될 때까지 병렬 대기 (성능 최적화)
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        
        try {
            allOf.join(); // 모든 업로드 완료 대기
            
            // 완료된 결과들을 수집
            return futures.stream()
                    .map(future -> {
                        try {
                            return future.join();
                        } catch (Exception e) {
                            log.error("이미지 업로드 실패", e);
                            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
                        }
                    })
                    .toList();
        } catch (Exception e) {
            log.error("병렬 이미지+썸네일 업로드 중 오류 발생", e);
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }
}
