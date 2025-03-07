package com.lareb.springProject.AirBnb.repository;

import com.lareb.springProject.AirBnb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
