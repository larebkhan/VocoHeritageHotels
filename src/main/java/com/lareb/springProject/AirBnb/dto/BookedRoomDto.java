package com.lareb.springProject.AirBnb.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookedRoomDto {
    private Long id;
    private RoomDto room;
    private int roomsCount;
    private BigDecimal pricePerRoom;
    private BigDecimal totalPrice;
}
