package org.example.cdio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ProductInventoryView {

    private Long productId;
    private String productName;
    private Integer weightGrams;
    private BigDecimal wholesalePrice;
    private Integer quantity;
    private LocalDateTime createdAt;
    private Integer expiryDays;
    private LocalDateTime expiredAt;
    private Long daysLeft;
}