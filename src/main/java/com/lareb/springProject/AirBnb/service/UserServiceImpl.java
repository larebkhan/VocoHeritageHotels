package com.lareb.springProject.AirBnb.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.lareb.springProject.AirBnb.entity.User;
import com.lareb.springProject.AirBnb.exception.ResourceNotFoundException;
import com.lareb.springProject.AirBnb.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
// Implement UserDetailsService
public class UserServiceImpl implements UserService, UserDetailsService {
    
    private final UserRepository userRepository;

    @Override
    public User getUserById(Long id) {
        // TODO Auto-generated method stub
        return userRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("User not found with ID: " + id));
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
