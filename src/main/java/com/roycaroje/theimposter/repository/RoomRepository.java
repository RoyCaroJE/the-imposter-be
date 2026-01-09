package com.roycaroje.theimposter.repository;

import com.roycaroje.theimposter.graphql.room.RoomDTO;
import com.roycaroje.theimposter.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByPublicId(UUID publicId);
    Optional<Room> findByCode(String code);
}