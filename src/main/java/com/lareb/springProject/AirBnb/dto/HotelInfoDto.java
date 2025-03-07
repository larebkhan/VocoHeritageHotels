package com.lareb.springProject.AirBnb.dto;

import com.lareb.springProject.AirBnb.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class HotelInfoDto {

    private HotelDto hotel;

    private List<RoomDto> rooms;
}
