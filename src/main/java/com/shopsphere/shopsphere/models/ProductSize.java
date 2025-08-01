package com.shopsphere.shopsphere.models;

import com.shopsphere.shopsphere.enums.Size;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "product_sizes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSize {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "size_id", updatable = false, nullable = false)
    private UUID sizeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Size size;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer stockForSize = 0;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product;
} 