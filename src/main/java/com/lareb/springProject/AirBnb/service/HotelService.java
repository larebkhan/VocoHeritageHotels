package com.lareb.springProject.AirBnb.service;

import com.lareb.springProject.AirBnb.dto.HotelDto;
import com.lareb.springProject.AirBnb.dto.HotelInfoDto;
import com.lareb.springProject.AirBnb.entity.Hotel;

public interface HotelService {
    HotelDto createNewHotel(HotelDto hotelDto);
    HotelDto getHotelById(Long hotelId);
    HotelDto updateHotelById(HotelDto hotelDto, Long hotelId);
    void deleteHotelByID(Long hotelId);
    void activateHotel(Long Hotel);

    HotelInfoDto getInfoById(Long hotelId);
}
