package com.shopsphere.shopsphere.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface CloudinaryService {
    Map<String, String> uploadImage(MultipartFile file) throws IOException;

    boolean deleteImage(String publicId) throws IOException;

    String extractPublicIdFromUrl(String imageUrl);
}