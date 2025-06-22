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
import com.ceos.beatbuddy.global.code.ErrorCode;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        validationImage(image.getOriginalFilename());
        return uploadImageS3(image, getBucketName(type), folder);
    }

    public List<String> uploadImages(List<MultipartFile> images, BucketType type, String directory) {
        return images.stream().map(image -> {
            try {
                return this.upload(image, type, directory);
            } catch (IOException e) {
                throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
            }
        }).toList();
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

    private String generateFileName(String originalFilename) {
        String extension = "";

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
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
