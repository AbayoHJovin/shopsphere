package com.shopsphere.shopsphere.dto.response;

import com.shopsphere.shopsphere.enums.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductSizeResponse {
    private UUID sizeId;
    private Size size;
    private Integer stockForSize;
} 