package org.example.cdio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import lombok.Data;

@Data
@AllArgsConstructor
public class ProductInventoryView {

    private Long productId;
    private String productName;
    private Integer weightGrams;
    private Double wholesalePrice;
    private Integer quantity;
    private LocalDateTime createdAt;
    private Integer expiryDays;
    private LocalDateTime expiredAt;
    private Long daysLeft;

}
