package com.tetgift.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

public interface CloudinaryService {
    String uploadFile(MultipartFile file) throws IOException;
    Map<?, ?> uploadFileWithInfo(MultipartFile file) throws IOException;
    void deleteFile(String publicId) throws IOException;
}
