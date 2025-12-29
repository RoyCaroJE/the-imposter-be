package com.roycaroje.theimposter.repository;

import com.roycaroje.theimposter.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByPublicId(UUID publicId);

    List<Player> findByRoomPublicId(UUID roomPublicId);

    List<Player> findByRoomPublicIdAndConfirmedTrue(UUID roomPublicId);
}

