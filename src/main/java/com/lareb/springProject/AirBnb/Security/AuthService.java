package com.lareb.springProject.AirBnb.Security;

import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.lareb.springProject.AirBnb.dto.LoginDto;
import com.lareb.springProject.AirBnb.dto.LoginResponseDto;
import com.lareb.springProject.AirBnb.dto.SignUpRequestDto;
import com.lareb.springProject.AirBnb.dto.UserDto;
import com.lareb.springProject.AirBnb.entity.User;
import com.lareb.springProject.AirBnb.entity.enums.Role;
import com.lareb.springProject.AirBnb.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public UserDto signUp(SignUpRequestDto signUpRequestDto) {
        User user = userRepository.findByEmail(signUpRequestDto.getEmail()).orElse(null);
        if (user != null) {
            throw new RuntimeException("User already exists with email: " + signUpRequestDto.getEmail());
        }
        User newUser = modelMapper.map(signUpRequestDto, User.class);
        newUser.setRoles(Set.of(Role.GUEST));
        newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
        newUser = userRepository.save(newUser);

        return modelMapper.map(newUser, UserDto.class);
    }

    public String[] login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
        
        User user = (User) authentication.getPrincipal();
        String arr[] = new String[2];
        arr[0] = jwtService.generateAccessToken(user);
        arr[1] = jwtService.generateRefreshToken(user);
        return arr;

    }

    
}