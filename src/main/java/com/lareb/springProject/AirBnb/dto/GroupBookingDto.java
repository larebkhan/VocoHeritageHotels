package com.lareb.springProject.AirBnb.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupBookingDto {

    private List<BookingDto> bookings;
    private BigDecimal totalAmount;
}
