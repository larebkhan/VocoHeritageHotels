package com.lareb.springProject.AirBnb.service;

import java.util.Optional;

import com.lareb.springProject.AirBnb.dto.ProfileUpdateRequestDto;
import com.lareb.springProject.AirBnb.dto.UserDto;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.lareb.springProject.AirBnb.entity.User;
import com.lareb.springProject.AirBnb.exception.ResourceNotFoundException;
import com.lareb.springProject.AirBnb.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.ui.ModelMap;

import static com.lareb.springProject.AirBnb.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
// Implement UserDetailsService
public class UserServiceImpl implements UserService, UserDetailsService {
    
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public User getUserById(Long id) {
        // TODO Auto-generated method stub
        return userRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("User not found with ID: " + id));
    }

    @Override
    public void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto) {
        User user = getCurrentUser();
        if(profileUpdateRequestDto.getDateOfBirth()!=null) user.setDateOfBirth(profileUpdateRequestDto.getDateOfBirth());
        if(profileUpdateRequestDto.getName()!=null) user.setName(profileUpdateRequestDto.getName());
        if(profileUpdateRequestDto.getGender()!=null) user.setGender(profileUpdateRequestDto.getGender());

        userRepository.save(user);

    }

    @Override
    public UserDto getMyProfile() {
        User user = getCurrentUser();
        return modelMapper.map(user,UserDto.class);
    }

    // Implementation of UserDetailsService method
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Assuming username is the email address
        return userRepository.findByEmail(username)
            .orElseThrow(() -> 
                new UsernameNotFoundException("User not found with email: " + username));
    }

    
}
