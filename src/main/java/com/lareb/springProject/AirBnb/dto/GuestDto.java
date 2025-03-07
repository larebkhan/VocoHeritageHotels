package com.lareb.springProject.AirBnb.dto;

import com.lareb.springProject.AirBnb.entity.User;
import com.lareb.springProject.AirBnb.entity.enums.Gender;
import jakarta.persistence.*;

public class GuestDto {

    private Long id;
    private User user;
    private String name;
    private Gender gender;
    private Integer age;


}
