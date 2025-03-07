package com.lareb.springProject.AirBnb.service;

import com.lareb.springProject.AirBnb.dto.HotelDto;
import com.lareb.springProject.AirBnb.dto.HotelPriceDto;
import com.lareb.springProject.AirBnb.dto.HotelSearchRequest;
import com.lareb.springProject.AirBnb.entity.Room;
import org.springframework.data.domain.Page;

public interface InventoryService {
    void initializeRoomForAYear(Room roomId);

    void deleteAllInventories(Room room);

    Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);
}
