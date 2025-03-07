package com.lareb.springProject.AirBnb.service;

import com.lareb.springProject.AirBnb.dto.RoomDto;
import com.lareb.springProject.AirBnb.entity.Room;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

public interface RoomService {
    RoomDto createNewRoom(Long hotelId,RoomDto roomDto);
    List<RoomDto> getAllRoomsInHotel(Long hotelId);
    RoomDto getRoomById(Long roomId);
    void deleteRoomById(Long roomId);
}
