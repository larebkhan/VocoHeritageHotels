package com.lareb.springProject.AirBnb.entity;


import com.lareb.springProject.AirBnb.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @OneToOne(fetch = FetchType.LAZY)
    private Booking booking;
}
