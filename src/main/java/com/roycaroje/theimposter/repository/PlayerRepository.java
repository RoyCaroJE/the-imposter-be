package com.roycaroje.theimposter.repository;

import com.roycaroje.theimposter.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}
