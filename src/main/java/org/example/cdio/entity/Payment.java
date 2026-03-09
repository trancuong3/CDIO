package org.example.cdio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Method method;

    private String gatewayTxnId;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime paidAt;

    public enum Method {
        GATEWAY,
        TRANSFER,
        CASH
    }

    public enum Status {
        PENDING,
        SUCCESS,
        FAILED
    }
}