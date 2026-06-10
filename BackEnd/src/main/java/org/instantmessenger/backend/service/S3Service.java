package org.instantmessenger.backend.service;

import org.instantmessenger.backend.dto.PresignedUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Presigner presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.presigned-url-expiry-minutes:5}")
    private int expiryMinutes;

    public S3Service(S3Presigner presigner) {
        this.presigner = presigner;
    }

    public PresignedUrlResponse generatePresignedUrl(String fileName, String contentType) {
        String safeContentType = (contentType != null && !contentType.isBlank())
                ? contentType : "application/octet-stream";
        String key = UUID.randomUUID() + "/" + fileName;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(safeContentType)
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(r -> r
                .signatureDuration(Duration.ofMinutes(expiryMinutes))
                .putObjectRequest(putRequest));

        String publicUrl = "https://%s.s3.%s.amazonaws.com/%s".formatted(bucket, region, key);
        return new PresignedUrlResponse(presigned.url().toString(), publicUrl);
    }
}
