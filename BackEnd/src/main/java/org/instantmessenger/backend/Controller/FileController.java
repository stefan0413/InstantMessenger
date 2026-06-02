package org.instantmessenger.backend.Controller;

import org.instantmessenger.backend.DTO.PresignedUrlResponse;
import org.instantmessenger.backend.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/files")
public class FileController {

    private final S3Service s3Service;

    public FileController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
            @RequestParam String fileName,
            @RequestParam(required = false) String contentType) {
        return ResponseEntity.ok(s3Service.generatePresignedUrl(fileName, contentType));
    }
}
