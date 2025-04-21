package com.lareb.springProject.AirBnb.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookingRequest {
    //when we book a hotel we need the info of what we want
    private Long hotelId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private List<RoomBookingRequest> rooms;

    @Getter
    @Setter
    public static class RoomBookingRequest {

        private Long roomId;
        private int roomsCount;
    }

}

