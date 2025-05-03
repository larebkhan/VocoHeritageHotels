package com.lareb.springProject.AirBnb.service;

import com.lareb.springProject.AirBnb.dto.*;
import com.lareb.springProject.AirBnb.entity.Hotel;
import com.lareb.springProject.AirBnb.entity.Inventory;
import com.lareb.springProject.AirBnb.entity.Room;
import com.lareb.springProject.AirBnb.entity.User;
import com.lareb.springProject.AirBnb.exception.ResourceNotFoundException;
import com.lareb.springProject.AirBnb.repository.HotelMinPriceRepository;
import com.lareb.springProject.AirBnb.repository.HotelRepository;
import com.lareb.springProject.AirBnb.repository.InventoryRepository;
import com.lareb.springProject.AirBnb.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.lareb.springProject.AirBnb.util.AppUtils.getCurrentUser;

@RequiredArgsConstructor
@Slf4j
@Service
public class InventoryServiceImpl implements InventoryService {
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final HotelMinPriceRepository hotelMinPriceRepository;



    @Override
    public void initializeRoomForAYear(Room room) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);
        for(; !today.isAfter(endDate);today = today.plusDays(1)){
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .reservedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    //changed
                    .build();

            if (inventory.getReservedCount() == null) {
                inventory.setReservedCount(0);
            }
            inventoryRepository.save(inventory);
        }
    }

    @Override
    public void deleteAllInventories(Room room) {
        log.info("Deleting the inventories of room with id: {}", room.getId());
        inventoryRepository.deleteByRoom(room);

    }

    @Override
    public Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest) {
        log.info("Searching hotels for {} city , from {} to {}", hotelSearchRequest.getCity(), hotelSearchRequest.getStartDate(), hotelSearchRequest.getEndDate());
        Pageable pageable = PageRequest.of(hotelSearchRequest.getPage(),
                hotelSearchRequest.getSize());

        long dateCount = ChronoUnit.DAYS.between(hotelSearchRequest.getStartDate(),
                hotelSearchRequest.getEndDate())+1;

        log.info("City: {}", hotelSearchRequest.getCity());
        log.info("Start Date: {}", hotelSearchRequest.getStartDate());
        log.info("End Date: {}", hotelSearchRequest.getEndDate());
        log.info("Rooms Count: {}", hotelSearchRequest.getRoomsCount());
        log.info("Date Count: {}", dateCount);


        Page<HotelPriceDto> hotelPage = hotelMinPriceRepository.findHotelsWithAvailableInventory(
                hotelSearchRequest.getCity(),
                hotelSearchRequest.getStartDate(),
                hotelSearchRequest.getEndDate(),
                hotelSearchRequest.getRoomsCount(),
                dateCount,
                pageable);

        return hotelPage;


    }

    @Override
    public List<InventoryDto> getAllInventoryByRoom(Long roomId) {
        log.info("Getting all inventory by room for room id : "+ roomId);
        Room room = roomRepository.findById(roomId).orElseThrow(()-> new ResourceNotFoundException("Room not found with id : "+ roomId));
        User user = getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())) throw new AccessDeniedException("Room doesnot belong to your hotel");

        List<Inventory> inventory = inventoryRepository.findByRoomOrderByDate(room);

        return inventory
                .stream()
                .map((element) -> modelMapper.map(element, InventoryDto.class)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto) {
        log.info("Updating all inventory by room for room id : "+ roomId);
        Room room = roomRepository.findById(roomId).orElseThrow(()-> new ResourceNotFoundException("Room not found with id : "+ roomId));
        User user = getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())) throw new AccessDeniedException("Room doesnot belong to your hotel");

        inventoryRepository.getInventoryAndLockBeforeUpdate(roomId,updateInventoryRequestDto.getStartDate(),updateInventoryRequestDto.getEndDate());
        inventoryRepository.updateInventory(roomId,updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate(),
                updateInventoryRequestDto.getClosed(),
                updateInventoryRequestDto.getSurgeFactor()
        );

    }
}
