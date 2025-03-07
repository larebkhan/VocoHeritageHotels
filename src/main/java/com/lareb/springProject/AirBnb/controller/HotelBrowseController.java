package com.lareb.springProject.AirBnb.controller;

import com.lareb.springProject.AirBnb.dto.HotelDto;
import com.lareb.springProject.AirBnb.dto.HotelInfoDto;
import com.lareb.springProject.AirBnb.dto.HotelPriceDto;
import com.lareb.springProject.AirBnb.dto.HotelSearchRequest;
import com.lareb.springProject.AirBnb.repository.HotelMinPriceRepository;
import com.lareb.springProject.AirBnb.service.HotelService;
import com.lareb.springProject.AirBnb.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;
    private final HotelMinPriceRepository hotelMinPriceRepository;

    @GetMapping("/search")
    private ResponseEntity<Page<HotelPriceDto>> searchHotels(@RequestBody HotelSearchRequest hotelSearchRequest){
        var page =  inventoryService.searchHotels(hotelSearchRequest);
        return ResponseEntity.ok(page);

    }

    @GetMapping("/{hotelId}/info")
    private ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable Long hotelId){

        return ResponseEntity.ok(hotelService.getInfoById(hotelId));
    }
}
