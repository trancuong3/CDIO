package org.example.cdio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    private StockType type;

    private Integer quantity;

    @Column(name = "trans_at")
    private LocalDateTime transAt;

    private String note;

    @PrePersist
    void init() {
        transAt = LocalDateTime.now();
    }
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

}
