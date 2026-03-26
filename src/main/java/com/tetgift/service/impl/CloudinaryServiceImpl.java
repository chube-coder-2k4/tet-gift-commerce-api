package com.tetgift.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.tetgift.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        return uploadFile(file, "general");
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        validateImageFile(file);

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image",
                "transformation", new Transformation().quality("auto")
        ));

        String secureUrl = (String) uploadResult.get("secure_url");
        log.info("Image uploaded to Cloudinary: folder={}, url={}", folder, secureUrl);
        return secureUrl;
    }

    @Override
    public Map<?, ?> uploadFileWithInfo(MultipartFile file, String folder) throws IOException {
        validateImageFile(file);

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image",
                "transformation", new Transformation().quality("auto")
        ));

        log.info("Image uploaded to Cloudinary: folder={}, public_id={}", folder, uploadResult.get("public_id"));
        return uploadResult;
    }

    @Override
    public void deleteFile(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        log.info("Image deleted from Cloudinary: public_id={}", publicId);
    }

    @Override
    public String uploadMusicFile(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("File cannot be empty");
        // Cloudinary uses 'video' resource_type for audio/video files
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "video"
        ));
        String secureUrl = (String) uploadResult.get("secure_url");
        log.info("Music uploaded to Cloudinary: folder={}, url={}", folder, secureUrl);
        return secureUrl;
    }

    @Override

    public void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size (10MB)");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException(
                    "Invalid file type: " + file.getContentType() + ". Only JPEG, PNG, GIF, and WebP are allowed");
        }
    }
}
