package com.roycaroje.theimposter.repository;

import com.roycaroje.theimposter.graphql.player.PlayerDTO;
import com.roycaroje.theimposter.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByPublicId(UUID publicId);

    List<Player> findByRoomPublicId(UUID roomPublicId);

    List<Player> findByRoomPublicIdAndConfirmedTrue(UUID roomPublicId);

    @Query("SELECT new com.roycaroje.theimposter.graphql.player.PlayerDTO(p.publicId, p.name, p.admin, p.confirmed, p.connected, p.roomPublicId) FROM Player p WHERE p.roomPublicId = :roomPublicId")
    List<PlayerDTO> findPlayersDTOByRoomPublicId(UUID roomPublicId);

    List<Player> findByConnectedTrue();
}

