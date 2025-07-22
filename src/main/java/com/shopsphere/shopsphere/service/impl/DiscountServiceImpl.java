package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.request.DiscountCreateRequest;
import com.shopsphere.shopsphere.dto.request.DiscountUpdateRequest;
import com.shopsphere.shopsphere.dto.response.DiscountResponse;
import com.shopsphere.shopsphere.dto.response.ProductSummaryResponse;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.models.Discount;
import com.shopsphere.shopsphere.models.Product;
import com.shopsphere.shopsphere.models.ProductImage;
import com.shopsphere.shopsphere.repository.DiscountRepository;
import com.shopsphere.shopsphere.repository.ProductRepository;
import com.shopsphere.shopsphere.service.DiscountService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public DiscountResponse createDiscount(DiscountCreateRequest request) {
        log.info("Creating new discount: {}", request.getName());

        // Fetch products
        List<Product> products = getProductsFromIds(request.getProductIds());

        // Create discount entity
        Discount discount = Discount.builder()
                .name(request.getName())
                .description(request.getDescription())
                .percentage(request.getPercentage())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(request.isActive())
                .products(products)
                .build();

        Discount savedDiscount = discountRepository.save(discount);
        return mapDiscountToResponse(savedDiscount);
    }

    @Override
    @Transactional
    public DiscountResponse updateDiscount(UUID discountId, DiscountUpdateRequest request) {
        log.info("Updating discount with ID: {}", discountId);

        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with ID: " + discountId));

        // Update fields if provided
        if (request.getName() != null) {
            discount.setName(request.getName());
        }

        if (request.getDescription() != null) {
            discount.setDescription(request.getDescription());
        }

        if (request.getPercentage() != null) {
            discount.setPercentage(request.getPercentage());
        }

        if (request.getStartDate() != null) {
            discount.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            discount.setEndDate(request.getEndDate());
        }

        if (request.getActive() != null) {
            discount.setActive(request.getActive());
        }

        // Update products if provided
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            List<Product> products = getProductsFromIds(request.getProductIds());
            discount.setProducts(products);
        }

        discount.setUpdatedAt(LocalDateTime.now());
        Discount updatedDiscount = discountRepository.save(discount);

        return mapDiscountToResponse(updatedDiscount);
    }

    @Override
    public DiscountResponse getDiscountById(UUID discountId) {
        log.info("Fetching discount with ID: {}", discountId);

        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with ID: " + discountId));

        return mapDiscountToResponse(discount);
    }

    @Override
    public Page<DiscountResponse> getAllDiscounts(Pageable pageable) {
        log.info("Fetching all discounts with pagination");

        return discountRepository.findAll(pageable)
                .map(this::mapDiscountToResponse);
    }

    @Override
    public Page<DiscountResponse> getActiveDiscounts(Pageable pageable) {
        log.info("Fetching active discounts with pagination");

        return discountRepository.findByActiveTrue(pageable)
                .map(this::mapDiscountToResponse);
    }

    @Override
    public Page<DiscountResponse> getDiscountsByProduct(UUID productId, Pageable pageable) {
        log.info("Fetching discounts for product with ID: {}", productId);

        // Verify product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with ID: " + productId);
        }

        return discountRepository.findByProductId(productId, pageable)
                .map(this::mapDiscountToResponse);
    }

    @Override
    @Transactional
    public void deleteDiscount(UUID discountId) {
        log.info("Deleting discount with ID: {}", discountId);

        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with ID: " + discountId));

        discountRepository.delete(discount);
    }

    @Override
    @Transactional
    public DiscountResponse activateDiscount(UUID discountId) {
        log.info("Activating discount with ID: {}", discountId);

        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with ID: " + discountId));

        discount.setActive(true);
        discount.setUpdatedAt(LocalDateTime.now());
        Discount updatedDiscount = discountRepository.save(discount);

        return mapDiscountToResponse(updatedDiscount);
    }

    @Override
    @Transactional
    public DiscountResponse deactivateDiscount(UUID discountId) {
        log.info("Deactivating discount with ID: {}", discountId);

        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with ID: " + discountId));

        discount.setActive(false);
        discount.setUpdatedAt(LocalDateTime.now());
        Discount updatedDiscount = discountRepository.save(discount);

        return mapDiscountToResponse(updatedDiscount);
    }

    @Override
    public List<DiscountResponse> getCurrentDiscountsForProduct(UUID productId) {
        log.info("Fetching current discounts for product with ID: {}", productId);

        // Verify product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with ID: " + productId);
        }

        LocalDateTime now = LocalDateTime.now();
        List<Discount> discounts = discountRepository.findCurrentDiscountsByProductId(productId, now);

        return discounts.stream()
                .map(this::mapDiscountToResponse)
                .collect(Collectors.toList());
    }

    // Scheduled task to deactivate expired discounts
    @Scheduled(cron = "0 0 0 * * *") // Run at midnight every day
    @Transactional
    public void deactivateExpiredDiscounts() {
        log.info("Running scheduled task to deactivate expired discounts");

        LocalDateTime now = LocalDateTime.now();
        List<Discount> expiredDiscounts = discountRepository.findByEndDateBeforeAndActiveTrue(now);

        for (Discount discount : expiredDiscounts) {
            discount.setActive(false);
            discount.setUpdatedAt(now);
            log.info("Deactivated expired discount: {}", discount.getName());
        }

        discountRepository.saveAll(expiredDiscounts);
    }

    // Helper methods
    private List<Product> getProductsFromIds(List<UUID> productIds) {
        return productIds.stream()
                .map(id -> productRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id)))
                .collect(Collectors.toList());
    }

    private DiscountResponse mapDiscountToResponse(Discount discount) {
        LocalDateTime now = LocalDateTime.now();
        boolean isExpired = discount.getEndDate().isBefore(now);
        boolean isCurrent = discount.isActive() && discount.getStartDate().isBefore(now)
                && discount.getEndDate().isAfter(now);

        List<ProductSummaryResponse> productResponses = discount.getProducts().stream()
                .map(product -> {
                    // Get main image URL if available
                    String imageUrl = product.getImages().stream()
                            .filter(ProductImage::getIsMain)
                            .findFirst()
                            .map(ProductImage::getImageUrl)
                            .orElse(null);

                    return ProductSummaryResponse.builder()
                            .productId(product.getProductId())
                            .name(product.getName())
                            .imageUrl(imageUrl)
                            .price(product.getPrice())
                            .stock(product.getStock())
                            .build();
                })
                .collect(Collectors.toList());

        return DiscountResponse.builder()
                .discountId(discount.getDiscountId())
                .name(discount.getName())
                .description(discount.getDescription())
                .percentage(discount.getPercentage())
                .startDate(discount.getStartDate())
                .endDate(discount.getEndDate())
                .active(discount.isActive())
                .createdAt(discount.getCreatedAt())
                .updatedAt(discount.getUpdatedAt())
                .products(productResponses)
                .expired(isExpired)
                .current(isCurrent)
                .build();
    }
}