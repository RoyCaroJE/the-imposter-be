package com.roycaroje.theimposter.repository;

import com.roycaroje.theimposter.graphql.room.PublicRoomsDTO;
import com.roycaroje.theimposter.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByPublicId(UUID publicId);
    Optional<Room> findByCode(String code);
    List<Room> findByCreatedBefore(Instant instant);

    @Query("SELECT new com.roycaroje.theimposter.graphql.room.PublicRoomsDTO(r.publicId, r.code, p.name, r.numImpostors, r.language, r.difficulty) FROM Room r JOIN Player p ON r.adminPublicId = p.publicId WHERE r.privateRoom IS NOT TRUE")
    List<PublicRoomsDTO> findAllPublicRooms();
}