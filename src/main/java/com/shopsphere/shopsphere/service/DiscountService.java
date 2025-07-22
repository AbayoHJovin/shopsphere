package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.request.DiscountCreateRequest;
import com.shopsphere.shopsphere.dto.request.DiscountUpdateRequest;
import com.shopsphere.shopsphere.dto.response.DiscountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface DiscountService {

    DiscountResponse createDiscount(DiscountCreateRequest request);

    DiscountResponse updateDiscount(UUID discountId, DiscountUpdateRequest request);

    DiscountResponse getDiscountById(UUID discountId);

    Page<DiscountResponse> getAllDiscounts(Pageable pageable);

    Page<DiscountResponse> getActiveDiscounts(Pageable pageable);

    Page<DiscountResponse> getDiscountsByProduct(UUID productId, Pageable pageable);

    void deleteDiscount(UUID discountId);

    DiscountResponse activateDiscount(UUID discountId);

    DiscountResponse deactivateDiscount(UUID discountId);

    List<DiscountResponse> getCurrentDiscountsForProduct(UUID productId);
}