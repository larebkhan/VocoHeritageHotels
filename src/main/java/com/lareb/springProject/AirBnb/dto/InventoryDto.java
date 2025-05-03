package com.lareb.springProject.AirBnb.dto;

import com.lareb.springProject.AirBnb.entity.Hotel;
import com.lareb.springProject.AirBnb.entity.Room;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InventoryDto {

    private Long id;
    private LocalDate date;
    private Integer bookedCount;
    private Integer reservedCount = 0;
    private Integer totalCount;
    private BigDecimal surgeFactor;
    private BigDecimal price; //basePrice * surgeFactor
    private Boolean closed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
