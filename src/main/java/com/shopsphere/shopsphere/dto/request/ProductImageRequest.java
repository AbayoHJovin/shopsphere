package com.shopsphere.shopsphere.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductImageRequest {
    private MultipartFile image;
    private Boolean isMain;
    private Integer position;
}