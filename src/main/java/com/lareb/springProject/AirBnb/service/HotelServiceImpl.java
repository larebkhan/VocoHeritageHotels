package com.lareb.springProject.AirBnb.service;

import com.lareb.springProject.AirBnb.dto.BookingDto;
import com.lareb.springProject.AirBnb.dto.HotelDto;
import com.lareb.springProject.AirBnb.dto.HotelInfoDto;
import com.lareb.springProject.AirBnb.dto.RoomDto;
import com.lareb.springProject.AirBnb.entity.Booking;
import com.lareb.springProject.AirBnb.entity.Hotel;
import com.lareb.springProject.AirBnb.entity.Room;
import com.lareb.springProject.AirBnb.entity.User;
import com.lareb.springProject.AirBnb.exception.ResourceNotFoundException;
import com.lareb.springProject.AirBnb.repository.BookingRepository;
import com.lareb.springProject.AirBnb.repository.HotelRepository;
import com.lareb.springProject.AirBnb.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;

import static com.lareb.springProject.AirBnb.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {
    private final BookingRepository bookingRepository;

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;
    private final BookingService bookingService;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("Creating a new Hotel with name: {}", hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
        hotel.setActive(false);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        hotel.setOwner(user);
        hotel = hotelRepository.save(hotel);
        log.info("Created a new Hotel with ID: {}", hotelDto.getId());
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long hotelId) {
        log.info("Getting the hotel with ID: {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID" + hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!hotel.getOwner().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Hotel not found with ID" + hotelId);
        }
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(HotelDto hotelDto, Long hotelId) {
        log.info("Updating the information of a Hotel that has Id : {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found for Id :" + hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!hotel.getOwner().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Hotel not found with ID" + hotelId);
        }
        modelMapper.map(hotelDto, hotel);
        hotel.setId(hotelId);
        hotel = hotelRepository.save(hotel);
        return modelMapper.map(hotel, HotelDto.class);

    }

    @Override
    @Transactional
    public void deleteHotelByID(Long hotelId) {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID" + hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!hotel.getOwner().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Hotel not found with ID" + hotelId);
        }

        for (Room room : hotel.getRooms()) {
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
        }

        hotelRepository.deleteById(hotelId);
        // for(Room room: hotel.getRooms()){
        // inventoryService.deleteAllInventories(room);
        // roomRepository.deleteById(room.getId());
        // }
    }

    @Override
    @Transactional
    public void activateHotel(Long hotelId) {
        log.info("Activating the Hotel that has Id : {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID" + hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!hotel.getOwner().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Hotel not found with ID" + hotelId);
        }

        hotel.setActive(true);
        // assuming only doing once
        for (Room room : hotel.getRooms()) {
            inventoryService.initializeRoomForAYear(room);
        }
    }

    @Override
    public HotelInfoDto getInfoById(Long hotelId) {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));
        List<RoomDto> rooms = hotel.getRooms().stream().map((element) -> modelMapper.map(element, RoomDto.class))
                .toList();

        return new HotelInfoDto(modelMapper.map(hotel, HotelDto.class), rooms);
    }

    @Override
    public List<HotelDto> getAllHotels() {
        User user = getCurrentUser();
        log.info("Getting all hotels for the admin user with id {}", user.getId());


        List<Hotel> hotel  = hotelRepository.findByOwner(user);
        return hotel.stream().map((element) -> modelMapper.map(element, HotelDto.class))
                .collect(Collectors.toList());
    }





}

// if you try to create an inventory fot the days or roomms that already has an
// inventory then it will throw an exception
//
