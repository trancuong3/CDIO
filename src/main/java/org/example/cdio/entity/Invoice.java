package org.example.cdio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter
@Setter
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "store_id")
    private Long storeId;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime issuedAt;

    public enum Status {
        UNPAID,
        PAID
    }
}