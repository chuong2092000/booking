package project.hotelservice.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@RefreshScope
public class S3UploadService {

    private final S3Client s3Client;

    @Value("${BUCKET_NAME}")
    private String bucket;

    @Value("${AWS_REGION}")
    private String region;

    private static final String MAIN_FOLDER = "booking";
    private static final String DEFAULT_IMAGE_KEY = MAIN_FOLDER + "/booking_default.webp";

    public enum Folder {
        HOTEL(MAIN_FOLDER + "/hotel"),
        ROOM_TYPE(MAIN_FOLDER + "/room-type");

        private final String path;

        Folder(String path) {
            this.path = path;
        }
    }

    public String uploadFile(MultipartFile file, String contentType, String key) {
        if (file == null || file.isEmpty()) {
            return getDefaultImageUrl();
        }
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType != null ? contentType : file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new RuntimeException("Error when upload file to S3: " + e.getMessage(), e);
        }

        return buildS3Url(key);
    }

    public List<String> uploadFiles(MultipartFile[] files, String contentType, Folder folder, List<UUID> randoms) {
        if (files == null || files.length == 0) {
            return List.of();
        }
        if (files.length != randoms.size()) {
            throw new IllegalArgumentException("Files quantity and UUID list size invalid!");
        }
        List<String> uploaded = new ArrayList<>();
        try {
            for (int i = 0; i < files.length; i++) {
                uploaded.add(uploadFile(files[i], contentType, buildKey(folder, randoms.get(i), files[i])));
            }
        } catch (Exception e) {
            deleteFilesByUrl(uploaded);
            throw new RuntimeException("Error when upload multiple files to S3: " + e.getMessage(), e);
        }
        return uploaded;
    }

    public void deleteFiles(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }

        List<ObjectIdentifier> objectIds = keys.stream()
                .filter(key -> key != null && !key.isBlank() && !key.contains(DEFAULT_IMAGE_KEY))
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .collect(Collectors.toList());

        if (objectIds.isEmpty()) {
            return;
        }

        try {
            s3Client.deleteObjects(DeleteObjectsRequest.builder()
                    .bucket(bucket)
                    .delete(Delete.builder().objects(objectIds).build())
                    .build());
        } catch (S3Exception e) {
            throw new RuntimeException("Error when delete file(s) on S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    public void deleteFilesByUrl(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }

        List<String> keys = fileUrls.stream()
                .filter(url -> url != null && !url.isBlank())
                .map(this::extractKeyFromUrl)
                .collect(Collectors.toList());

        deleteFiles(keys);
    }

    public String extractKeyFromUrl(String fileUrl) {
        try {
            String key = URLDecoder.decode(URI.create(fileUrl).getPath(), StandardCharsets.UTF_8);
            return key.startsWith("/") ? key.substring(1) : key;
        } catch (Exception e) {
            throw new RuntimeException("Invalid URL for delete file: " + fileUrl, e);
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return "." + originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
    }

    public String getHostUrl() {
        return String.format("https://%s.s3.%s.amazonaws.com", bucket, region);
    }

    public String buildS3Url(String key) {
        return getHostUrl() + "/" + key;
    }

    public String buildKey(Folder folder, UUID random, MultipartFile file) {
        return folder.path + "/" + random + extractExtension(file.getOriginalFilename());
    }

    private String getDefaultImageUrl() {
        return buildS3Url(DEFAULT_IMAGE_KEY);
    }
}