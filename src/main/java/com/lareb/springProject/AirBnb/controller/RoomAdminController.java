package com.lareb.springProject.AirBnb.controller;


import com.lareb.springProject.AirBnb.advice.ApiResponse;
import com.lareb.springProject.AirBnb.dto.RoomDto;
import com.lareb.springProject.AirBnb.entity.Room;
import com.lareb.springProject.AirBnb.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/hotels/{hotelId}/rooms")
@RequiredArgsConstructor
public class RoomAdminController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomDto> createNewRoom(@PathVariable Long hotelId, @RequestBody RoomDto roomDto){
        RoomDto newRoom = roomService.createNewRoom(hotelId, roomDto);
        return new ResponseEntity<>(newRoom, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomDto>>> getAllRoomsInHotel(@PathVariable  Long hotelId){
        return ResponseEntity.ok(new ApiResponse<>(roomService.getAllRoomsInHotel(hotelId)));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<?>> getRoomById(@PathVariable  Long roomId){
        return new ResponseEntity<>(new ApiResponse<>(roomService.getRoomById(roomId)),HttpStatus.FOUND);
    }


    @DeleteMapping("/{roomId}")
    public ResponseEntity<RoomDto> deleteRoomById(@PathVariable  Long roomId){
        roomService.deleteRoomById(roomId);
        return ResponseEntity.noContent().build();
    }

}
