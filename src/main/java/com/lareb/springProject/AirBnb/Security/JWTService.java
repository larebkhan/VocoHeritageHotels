package com.lareb.springProject.AirBnb.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.lareb.springProject.AirBnb.entity.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JWTService {

    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    private SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }
    @Transactional
     public String generateAccessToken(User user){
         String token =  Jwts.builder()
                 .subject(user.getId().toString())
                 .claim("email",user.getEmail())
                 .claim("roles", user.getRoles().toString())
                 .issuedAt(new Date())
                 .expiration(new Date(System.currentTimeMillis() + 1000*60*10))
                 .signWith(getSecretKey())
                 .compact();

         return token;

     }

    @Transactional
    public String generateRefreshToken(User user){
        String token =  Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000L *60*60*24*30*6))
                .signWith(getSecretKey())
                .compact();
        return token;

    }

     public Long getUserIdFromToken(String token) {
         try {
             System.out.println("Parsing token...");
             Claims claims = Jwts.parser()
                     .verifyWith(getSecretKey())
                     .build()
                     .parseSignedClaims(token)
                     .getPayload();
             
             System.out.println("Token Claims: " + claims);
             return Long.valueOf(claims.getSubject());
         } catch (Exception e) {
             System.out.println("Error parsing token: " + e.getMessage());
             throw e;
         }
     }
}
