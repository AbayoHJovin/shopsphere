package com.shopsphere.shopsphere.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "product_colors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductColor {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "color_id", updatable = false, nullable = false)
    private UUID colorId;

    @Column(nullable = false)
    private String colorName;
    
    @Column
    private String colorHexCode;
    
    // Removed the product reference
}
