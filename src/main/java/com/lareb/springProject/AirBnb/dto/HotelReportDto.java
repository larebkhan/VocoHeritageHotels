package com.lareb.springProject.AirBnb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelReportDto {

    private long bookingCount;
    private BigDecimal totalRevenue;
    private BigDecimal avgRevenue;

}
