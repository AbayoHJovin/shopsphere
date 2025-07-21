package com.shopsphere.shopsphere.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.shopsphere.shopsphere.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder}")
    private String folder;

    @Override
    public Map<String, String> uploadImage(MultipartFile file) throws IOException {
        try {
            // Generate a unique filename to avoid collisions
            String filename = UUID.randomUUID().toString();

            // Upload options
            Map<String, Object> options = new HashMap<>();
            options.put("public_id", folder + "/" + filename);
            options.put("overwrite", true);
            options.put("resource_type", "auto");

            // Upload file to Cloudinary
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);

            // Extract and return relevant information
            Map<String, String> result = new HashMap<>();
            result.put("public_id", (String) uploadResult.get("public_id"));
            result.put("url", (String) uploadResult.get("url"));
            result.put("secure_url", (String) uploadResult.get("secure_url"));

            log.info("Image uploaded successfully to Cloudinary: {}", result.get("public_id"));

            return result;
        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw e;
        }
    }

    @Override
    public boolean deleteImage(String publicId) throws IOException {
        try {
            // Delete the image from Cloudinary
            Map<?, ?> deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String result = (String) deleteResult.get("result");

            boolean success = "ok".equals(result);
            if (success) {
                log.info("Image deleted successfully from Cloudinary: {}", publicId);
            } else {
                log.warn("Failed to delete image from Cloudinary: {}, result: {}", publicId, result);
            }

            return success;
        } catch (IOException e) {
            log.error("Error deleting image from Cloudinary: {}", publicId, e);
            throw e;
        }
    }

    @Override
    public String extractPublicIdFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        // Extract the public ID from the Cloudinary URL
        // Example URL:
        // https://res.cloudinary.com/cloud-name/image/upload/v1234567890/shopsphere/users/abc123
        try {
            // Split by /upload/ to get the version and public ID part
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }

            // Get the part after version (v1234567890/)
            String versionAndPublicId = parts[1];

            // Extract the public ID by removing the version prefix if it exists
            String publicId;
            if (versionAndPublicId.matches("v\\d+/.*")) {
                publicId = versionAndPublicId.replaceFirst("v\\d+/", "");
            } else {
                publicId = versionAndPublicId;
            }

            return publicId;
        } catch (Exception e) {
            log.error("Error extracting public ID from URL: {}", imageUrl, e);
            return null;
        }
    }
}