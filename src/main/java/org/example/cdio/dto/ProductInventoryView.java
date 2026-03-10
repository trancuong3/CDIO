package org.example.cdio.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
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


    private String img;

    // constructor
    public ProductInventoryView(Long productId,
                                String productName,
                                Integer weightGrams,
                                BigDecimal wholesalePrice,
                                Integer quantity,
                                LocalDateTime createdAt,
                                Integer expiryDays,
                                LocalDateTime expiredAt,
                                Long daysLeft,
                                String img) {

        this.productId = productId;
        this.productName = productName;
        this.weightGrams = weightGrams;
        this.wholesalePrice = wholesalePrice;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.expiryDays = expiryDays;
        this.expiredAt = expiredAt;
        this.daysLeft = daysLeft;
        this.img = img;
    }


    public String getImg() {
        return img;
    }


}