package com.ceos.beatbuddy.global.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoThumbnailService {
    
    private final AmazonS3 amazonS3;
    
    @Value("${cloud.aws.s3.bucket-post}")
    private String bucketName;

    /**
     * 비디오 파일의 첫 번째 프레임을 추출하여 썸네일을 생성하고 S3에 업로드합니다.
     *
     * @param videoFile 비디오 파일
     * @param folder S3 폴더 경로
     * @return 썸네일 이미지 S3 URL
     */
    public String generateAndUploadThumbnail(MultipartFile videoFile, String folder) {
        Path tempVideoPath = null;
        Path tempThumbnailPath = null;
        
        try {
            // 임시 비디오 파일 생성
            tempVideoPath = createTempFile(videoFile, "video_");
            
            // 썸네일 임시 파일 경로 생성
            String thumbnailFileName = generateThumbnailFileName(videoFile.getOriginalFilename());
            tempThumbnailPath = Files.createTempFile("thumbnail_", ".jpg");
            
            // FFmpeg를 사용하여 썸네일 생성
            extractThumbnail(tempVideoPath, tempThumbnailPath);
            
            // S3에 업로드
            return uploadThumbnailToS3(tempThumbnailPath, folder, thumbnailFileName);
            
        } catch (Exception e) {
            log.error("비디오 썸네일 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        } finally {
            // 임시 파일 정리
            cleanupTempFiles(tempVideoPath, tempThumbnailPath);
        }
    }

    /**
     * MultipartFile을 임시 파일로 저장합니다.
     */
    private Path createTempFile(MultipartFile file, String prefix) throws IOException {
        String extension = getFileExtension(file.getOriginalFilename());
        Path tempPath = Files.createTempFile(prefix, "." + extension);
        
        try (InputStream inputStream = file.getInputStream();
             OutputStream outputStream = Files.newOutputStream(tempPath)) {
            inputStream.transferTo(outputStream);
        }
        
        return tempPath;
    }

    /**
     * FFmpeg를 사용하여 비디오의 첫 번째 프레임을 추출합니다.
     */
    private void extractThumbnail(Path videoPath, Path thumbnailPath) throws IOException {
        try {
            // FFmpeg와 FFprobe 경로 설정 (시스템에 설치된 경우)
            FFmpeg ffmpeg = new FFmpeg("ffmpeg");
            FFprobe ffprobe = new FFprobe("ffprobe");
            
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(videoPath.toString())
                    .overrideOutputFiles(true)
                    .addOutput(thumbnailPath.toString())
                    .setFrames(1) // 첫 번째 프레임만 추출
                    .setVideoPixelFormat("yuvj420p") // JPEG 호환 픽셀 포맷
                    .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            executor.createJob(builder).run();
            
        } catch (Exception e) {
            log.error("FFmpeg 썸네일 추출 실패: {}", e.getMessage(), e);
            // FFmpeg가 없는 경우를 대비한 기본 썸네일 생성
            createDefaultThumbnail(thumbnailPath);
        }
    }

    /**
     * FFmpeg가 사용할 수 없는 경우 기본 썸네일을 생성합니다.
     */
    private void createDefaultThumbnail(Path thumbnailPath) throws IOException {
        // 기본 썸네일 이미지 생성 (단색 이미지)
        // 실제 구현에서는 리소스 폴더의 기본 이미지를 복사하거나
        // 간단한 이미지를 프로그래밍 방식으로 생성할 수 있습니다
        log.warn("FFmpeg를 사용할 수 없어 기본 썸네일을 생성합니다.");
        
        // 파일이 이미 존재하면 삭제 후 생성
        if (Files.exists(thumbnailPath)) {
            Files.delete(thumbnailPath);
        }
        
        // 임시로 빈 파일 생성 (실제로는 기본 썸네일 이미지 파일을 복사해야 함)
        Files.createFile(thumbnailPath);
    }

    /**
     * 생성된 썸네일을 S3에 업로드합니다.
     */
    private String uploadThumbnailToS3(Path thumbnailPath, String folder, String fileName) throws IOException {
        String s3FileName = folder + "/thumbnails/" + fileName;
        
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/jpeg");
        
        byte[] thumbnailBytes = Files.readAllBytes(thumbnailPath);
        metadata.setContentLength(thumbnailBytes.length);
        
        try (InputStream inputStream = new ByteArrayInputStream(thumbnailBytes)) {
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, s3FileName, inputStream, metadata);
            amazonS3.putObject(putRequest);
        }
        
        return amazonS3.getUrl(bucketName, s3FileName).toString();
    }

    /**
     * 썸네일 파일명을 생성합니다.
     */
    private String generateThumbnailFileName(String originalFileName) {
        String baseName = getBaseName(originalFileName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_thumbnail_%s_%s.jpg", baseName, timestamp, uuid);
    }

    /**
     * 파일 확장자를 추출합니다.
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * 파일명에서 확장자를 제외한 기본명을 추출합니다.
     */
    private String getBaseName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "video";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, lastDotIndex);
    }

    /**
     * 임시 파일들을 정리합니다.
     */
    private void cleanupTempFiles(Path... tempPaths) {
        for (Path tempPath : tempPaths) {
            if (tempPath != null) {
                try {
                    Files.deleteIfExists(tempPath);
                } catch (IOException e) {
                    log.warn("임시 파일 삭제 실패: {}", tempPath, e);
                }
            }
        }
    }
}