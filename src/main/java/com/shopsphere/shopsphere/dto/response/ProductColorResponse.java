package com.shopsphere.shopsphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductColorResponse {
    private UUID colorId;
    private String colorName;
    private String colorHexCode;
    // Removed productId and productName fields as colors are now independent of products
}