package com.lareb.springProject.AirBnb.dto;

import com.lareb.springProject.AirBnb.entity.Guest;
import com.lareb.springProject.AirBnb.entity.Hotel;
import com.lareb.springProject.AirBnb.entity.Room;
import com.lareb.springProject.AirBnb.entity.User;
import com.lareb.springProject.AirBnb.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class BookingDto {

    private Long id;
    private Integer totalRoomsCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime createdAt;//if a user refreshes the page the timer can keep on updating in the front end
    private LocalDateTime updatedAt;
    private BookingStatus bookingStatus;
    private Set<GuestDto> guests;
    private Set<BookedRoomDto> bookedRooms;
    private BigDecimal amount;


}
