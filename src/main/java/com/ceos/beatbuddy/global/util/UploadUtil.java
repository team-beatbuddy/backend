package com.ceos.beatbuddy.global.util;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ceos.beatbuddy.domain.venue.exception.VenueErrorCode;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import jakarta.annotation.PostConstruct;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.apache.commons.compress.utils.FileNameUtils.getExtension;

@Component
public class UploadUtil {
    private static String bucketName;
    private static String beatbuddyBucketName;
    private static String accessKey;
    private static String secretKey;
    private static String region;

    private static AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    public void setBucketName(String value) {
        UploadUtil.bucketName = value;
    }

    @Value("${cloud.aws.s3.bucket-post}")
    public void setBeatbuddyBucketName(String value) {
        UploadUtil.beatbuddyBucketName = value;
    }

    @Value("${cloud.aws.credentials.access-key}")
    public void setAccessKey(String value) {
        UploadUtil.accessKey = value;
    }

    @Value("${cloud.aws.credentials.secret-key}")
    public void setSecretKey(String value) {
        UploadUtil.secretKey = value;
    }

    @Value("${cloud.aws.region.static}")
    public void setRegion(String value) {
        UploadUtil.region = value;
    }

    @PostConstruct
    private void init() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        amazonS3 = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }

    public String upload(MultipartFile image, BucketType type, String folder) {
        if (image.isEmpty() || Objects.isNull(image.getOriginalFilename())) {
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        // 확장자 및 파일명 검증
        validationImage(image.getOriginalFilename());

        return uploadImageS3(image, getBucketName(type), folder);
    }

    public String uploadThumbnail(MultipartFile image, BucketType type, String folder, String fileName) {
        String bucketName = getBucketName(type);
        String s3FileName = folder + "/thumbnail/" + fileName;

        ObjectMetadata metadata = new ObjectMetadata();
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            Thumbnails.of(image.getInputStream())
                    .scale(1.0)
                    .outputFormat("jpg")
                    .outputQuality(0.3) // 30% 품질
                    .toOutputStream(os);
        } catch (IOException e) {
            throw new CustomException("썸네일 생성 실패");
        }

        byte[] bytes = os.toByteArray();
        metadata.setContentLength(bytes.length);
        metadata.setContentType("image/jpeg");

        try (InputStream is = new ByteArrayInputStream(bytes)) {
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, s3FileName, is, metadata);
            amazonS3.putObject(putRequest);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return amazonS3.getUrl(bucketName, s3FileName).toString();
    }


    public List<String> uploadImages(List<MultipartFile> images, BucketType type, String folder) {
        return images.stream().map(image -> upload(image, type, folder)).toList();
    }


    private String uploadImageS3(MultipartFile image, String bucketName, String folder) throws UncheckedIOException {
        String fileName = generateFileName(Objects.requireNonNull(image.getOriginalFilename()));
        String s3FileName = (folder != null && !folder.isBlank()) ? folder + "/" + fileName : fileName;

        ObjectMetadata metadata = getObjectMetadata(image);

        try (InputStream is = image.getInputStream()) {
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, s3FileName, is, metadata);
            amazonS3.putObject(putRequest);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        return amazonS3.getUrl(bucketName, s3FileName).toString();
    }

    /**
     * 현재 시간의 타임스탬프, 랜덤 UUID, 그리고 원본 파일의 확장자를 이용하여 고유한 파일명을 생성합니다.
     *
     * @param originalFilename 원본 파일명 (확장자 추출에 사용됨)
     * @return "yyyyMMdd_HHmmss_UUID.확장자" 형식의 새로운 파일명
     */
    private String generateFileName(String originalFilename) {
        String extension = "";

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
            extension = originalFilename.substring(dotIndex);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString();

        return timestamp + "_" + uuid + extension;
    }


    private String getBucketName(BucketType type) {
        return switch (type) {
            case VENUE -> bucketName;
            case MEDIA -> beatbuddyBucketName;
        };
    }

    public enum BucketType {
        VENUE,
        MEDIA,
    }

    private static ObjectMetadata getObjectMetadata(MultipartFile image) {
        ObjectMetadata metadata = new ObjectMetadata(); //metadata 생성
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());
        return metadata;
    }

    /**
     * Validates that the provided filename contains a file extension.
     *
     * @param fileName the name of the file to validate
     * @throws CustomException if the filename does not contain a dot character, indicating a missing extension
     */
    private static void validationImage(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new CustomException(VenueErrorCode.INVALID_VENUE_IMAGE);
        }
    }

    /**
     * Deletes an image from the specified AWS S3 bucket based on its URL and bucket type.
     *
     * @param imageUrl the public URL of the image to delete
     * @param type the type of bucket from which to delete the image
     * @throws CustomException if the image deletion fails
     */
    public void deleteImage(String imageUrl, BucketType type) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        String bucketName = getBucketName(type);
        String bucketUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/";
        String key = imageUrl.replace(bucketUrl, "");

        try {
            amazonS3.deleteObject(bucketName, key);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.IMAGE_DELETE_FAILED);
        }
    }

    public void deleteImages(List<String> imageUrls, BucketType type) {
        if (imageUrls == null || imageUrls.isEmpty()) return;

        imageUrls.forEach(imageUrl -> this.deleteImage(imageUrl, type));
    }
}
