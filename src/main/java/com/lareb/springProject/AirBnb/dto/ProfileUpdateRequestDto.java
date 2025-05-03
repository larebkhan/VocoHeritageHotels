package com.lareb.springProject.AirBnb.dto;

import com.lareb.springProject.AirBnb.entity.enums.Gender;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileUpdateRequestDto {

    private String  name;
    private LocalDate dateOfBirth;
    private Gender gender;
}
