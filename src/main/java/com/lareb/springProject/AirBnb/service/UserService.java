package com.lareb.springProject.AirBnb.service;

import com.lareb.springProject.AirBnb.dto.ProfileUpdateRequestDto;
import com.lareb.springProject.AirBnb.dto.UserDto;
import com.lareb.springProject.AirBnb.entity.User;

public interface UserService {
    
    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();
}
