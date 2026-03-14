package com.tetgift.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.tetgift.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        File uploadedFile = convertMultiPartToFile(file);
        Map<?, ?> uploadResult = cloudinary.uploader().upload(uploadedFile, ObjectUtils.emptyMap());
        boolean isDeleted = uploadedFile.delete();
        if(!isDeleted){
            log.warn("File is not deleted");
        }
        return uploadResult.get("url").toString();
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    @Override
    public Map<?, ?> uploadFileWithInfo(MultipartFile file) throws IOException {
        File uploadedFile = convertMultiPartToFile(file);
        Map<?, ?> uploadResult = cloudinary.uploader().upload(uploadedFile, ObjectUtils.emptyMap());
        boolean isDeleted = uploadedFile.delete();
        if(!isDeleted){
            log.warn("File is not deleted");
        }
        return uploadResult;
    }

    @Override
    public void deleteFile(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
