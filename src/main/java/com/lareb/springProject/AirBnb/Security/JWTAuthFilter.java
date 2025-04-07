package com.lareb.springProject.AirBnb.Security;

import java.io.IOException;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.lareb.springProject.AirBnb.entity.User;
import com.lareb.springProject.AirBnb.service.UserService;

import org.springframework.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class JWTAuthFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserService userService;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)throws ServletException, IOException {
        // TODO Auto-generated method stub
        try{
            final String authHeader = request.getHeader("Authorization");
            if(authHeader == null || !authHeader.startsWith("Bearer ")){
                System.out.println("No Bearer token found, skipping authentication");
                // If no token is found, continue with the filter chain
                filterChain.doFilter(request, response);
                return;
            }
            String token = authHeader.split("Bearer ")[1];
            Long userId = jwtService.getUserIdFromToken(token);
            //Our token has userId, we can use it to authenticate the user
            // we have fetched the token from the request header
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userService.getUserById(userId);
            
                Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
                System.out.println("User Roles: " + user.getRoles());
                System.out.println("Authorities: " + authorities);
            

                UsernamePasswordAuthenticationToken authenticationToken = 
                    new UsernamePasswordAuthenticationToken(user, null, authorities);
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                System.out.println("Has ROLE_ADMIN: " + authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                System.out.println("Authentication token set in SecurityContext");
            }

            System.out.println("Final Authentication: " + SecurityContextHolder.getContext().getAuthentication());
            System.out.println("==== JWT Filter End ====");
            filterChain.doFilter(request,response);
        } catch (Exception ex){
            System.out.println("Error in JWT Filter: " + ex.getMessage());
            ex.printStackTrace();
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }
}
