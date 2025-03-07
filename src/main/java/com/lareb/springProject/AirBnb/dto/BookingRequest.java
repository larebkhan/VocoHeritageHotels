package com.lareb.springProject.AirBnb.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequest {
    //when we book a hotel we need the info of what we want
    private Long hotelId;
    private Long roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer roomsCount;
}

