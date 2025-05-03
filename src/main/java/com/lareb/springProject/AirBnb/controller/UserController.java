package com.lareb.springProject.AirBnb.controller;

import com.lareb.springProject.AirBnb.dto.BookingDto;
import com.lareb.springProject.AirBnb.dto.ProfileUpdateRequestDto;
import com.lareb.springProject.AirBnb.dto.UserDto;
import com.lareb.springProject.AirBnb.entity.User;
import com.lareb.springProject.AirBnb.service.BookingService;
import com.lareb.springProject.AirBnb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.lareb.springProject.AirBnb.util.AppUtils.getCurrentUser;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private  final UserService userService;
    private final BookingService bookingService;

    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody ProfileUpdateRequestDto profileUpdateRequestDto){
        userService.updateProfile(profileUpdateRequestDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/myBookings")
    public ResponseEntity<List<BookingDto>> getMyBookings(){
        return ResponseEntity.ok(bookingService.getMyBookings());
    }

    @GetMapping("/myProfile")
    public ResponseEntity<UserDto> getMyProfile(){
        return ResponseEntity.ok(userService.getMyProfile());
    }
}
