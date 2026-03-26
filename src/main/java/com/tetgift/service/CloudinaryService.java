package com.tetgift.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

public interface CloudinaryService {
    /**
     * Upload file to specific folder with validation
     * @param file MultipartFile to upload
     * @param folder Cloudinary folder (e.g. "products", "bundles", "blogs")
     * @return secure_url of uploaded file
     */
    String uploadFile(MultipartFile file, String folder) throws IOException;

    /**
     * Upload file to default folder
     */
    String uploadFile(MultipartFile file) throws IOException;

    /**
     * Upload file and return full Cloudinary response (url, public_id, etc.)
     */
    Map<?, ?> uploadFileWithInfo(MultipartFile file, String folder) throws IOException;

    /**
     * Delete file by publicId
     */
    void deleteFile(String publicId) throws IOException;

    /**
     * Upload music file (MP3) to specific folder
     */
    String uploadMusicFile(MultipartFile file, String folder) throws IOException;

    /**
     * Validate file before upload (type, size)
     */
    void validateImageFile(MultipartFile file);
}
