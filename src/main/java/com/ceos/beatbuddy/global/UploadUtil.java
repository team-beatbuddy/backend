package com.ceos.beatbuddy.global;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import com.ceos.beatbuddy.domain.venue.exception.VenueErrorCode;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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

    public String upload(MultipartFile image, BucketType type, String folder) throws IOException {
        if (image.isEmpty() || Objects.isNull(image.getOriginalFilename())) {
            throw new CustomException(VenueErrorCode.INVALID_VENUE_IMAGE);
        }

        validationImage(image.getOriginalFilename());
        return uploadImageS3(image, getBucketName(type), folder);
    }

    private String uploadImageS3(MultipartFile image, String bucketName, String folder) throws IOException {
        String s3FileName = (folder != null && !folder.isBlank())
                ? folder + "/" + generateFileName(image.getOriginalFilename())
                : generateFileName(image.getOriginalFilename());

        InputStream is = image.getInputStream();
        byte[] bytes = IOUtils.toByteArray(is);
        ObjectMetadata metadata = getObjectMetadata(image);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, s3FileName, byteArrayInputStream, metadata);
            amazonS3.putObject(putObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(VenueErrorCode.IMAGE_UPLOAD_FAILED);
        } finally {
            byteArrayInputStream.close();
            is.close();
        }

        return amazonS3.getUrl(bucketName, s3FileName).toString();
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

    private static String generateFileName(String originalFilename) {
        return UUID.randomUUID().toString().substring(0, 10) + originalFilename;
    }

    private static ObjectMetadata getObjectMetadata(MultipartFile image) {
        ObjectMetadata metadata = new ObjectMetadata(); //metadata 생성
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());
        return metadata;
    }

    private static void validationImage(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new CustomException(VenueErrorCode.INVALID_VENUE_IMAGE);
        }
    }

    private static void validateImageExtension(String fileName) {
        if (!fileName.contains(".")) {
            throw new CustomException(VenueErrorCode.INVALID_VENUE_IMAGE);
        }
    }

    public void delete(String imageUrl, BucketType type) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        String bucketName = getBucketName(type);
        String bucketUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/";
        String key = imageUrl.replace(bucketUrl, "");

        try {
            amazonS3.deleteObject(bucketName, key);
        } catch (Exception e) {
            e.printStackTrace(); // 또는 로그 처리
            // 삭제 실패 시 로직을 중단하진 않도록 처리
        }
    }
}
