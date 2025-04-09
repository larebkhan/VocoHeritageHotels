package com.lareb.springProject.AirBnb.service;

import com.lareb.springProject.AirBnb.dto.RoomDto;
import com.lareb.springProject.AirBnb.entity.Hotel;
import com.lareb.springProject.AirBnb.entity.Room;
import com.lareb.springProject.AirBnb.entity.User;
import com.lareb.springProject.AirBnb.exception.ResourceNotFoundException;
import com.lareb.springProject.AirBnb.repository.HotelRepository;
import com.lareb.springProject.AirBnb.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {
        log.info("Creating a new room with Id: {}", roomDto.getId());
        Room room = modelMapper.map(roomDto, Room.class);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID" + hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!hotel.getOwner().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Hotel not found with ID , as the user doesnot own it" + hotelId);
        }
        room.setHotel(hotel);
        room.setActive(true);// changed
        room = roomRepository.save(room);

        if (hotel.getActive()) {
            inventoryService.initializeRoomForAYear(room); 
        }
        log.info("Created a new room with ID: {}", roomDto.getId());
        return modelMapper.map(room, RoomDto.class);

    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all rooms for the hotel with Id {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID" + hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!hotel.getOwner().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Hotel not found with ID as the user doesnot own it" + hotelId);
        }
        List<Room> all = hotel.getRooms();
        List<RoomDto> collect = all.stream()
                .map(room -> modelMapper.map(room, RoomDto.class))
                .collect(Collectors.toList());
        return collect;

    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting rooms with Id {}", roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID as the user doesnot own it" + roomId));

        return modelMapper.map(room, RoomDto.class);

    }

    @Override
    @Transactional
    public void deleteRoomById(Long roomId) {
        log.info("Deleting room that has ID {}", +roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID" + roomId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!room.getHotel().getOwner().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Room not found with ID , as the user doesnot own it" + roomId);
        }

        inventoryService.deleteAllInventories(room);
        roomRepository.deleteById(roomId);

    }
}
