package com.lareb.springProject.AirBnb.util;

import com.lareb.springProject.AirBnb.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class AppUtils {
    public static User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Fetch the dummy user from the database

    }
}
