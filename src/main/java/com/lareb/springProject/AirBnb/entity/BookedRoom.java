package com.lareb.springProject.AirBnb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//This entity will link each room type and its quantity with a particular booking.
public class BookedRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private Integer roomsCount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerRoom;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
}
