package com.lareb.springProject.AirBnb.repository;

import com.lareb.springProject.AirBnb.dto.HotelPriceDto;
import com.lareb.springProject.AirBnb.entity.Hotel;
import com.lareb.springProject.AirBnb.entity.HotelMinPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

//stores the average price of a specific hotel in a given date range based on the data of HotelMinPrice table
public interface HotelMinPriceRepository extends JpaRepository<HotelMinPrice, Long> {


    @Query("""
            Select new com.lareb.springProject.AirBnb.dto.HotelPriceDto(i.hotel, AVG(i.price))
            FROM HotelMinPrice  i
            WHERE i.hotel.city = :city
                AND i.date BETWEEN :startDate AND :endDate
                AND i.hotel.active = true
            GROUP BY i.hotel
            """)
    Page<HotelPriceDto> findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount,
            Pageable pageable
    );

    Optional<HotelMinPrice> findByHotelAndDate(Hotel hotel, LocalDate date);
}
