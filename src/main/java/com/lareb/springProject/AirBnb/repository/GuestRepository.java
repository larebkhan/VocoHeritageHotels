package com.lareb.springProject.AirBnb.repository;

import com.lareb.springProject.AirBnb.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {
}
