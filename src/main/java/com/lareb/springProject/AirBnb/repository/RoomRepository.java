package com.lareb.springProject.AirBnb.repository;

import com.lareb.springProject.AirBnb.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
