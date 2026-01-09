package com.roycaroje.theimposter.transformer;

import com.roycaroje.theimposter.model.Player;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PlayerTransformer {

    public Player createAdminPlayer(String playerName, UUID roomPublicId){
        Player player = new Player();
        player.setPublicId(UUID.randomUUID());
        player.setAdmin(true);
        player.setName(playerName);
        player.setConnected(true);
        player.setRoomPublicId(roomPublicId);
        player.setImpostor(false);
        player.setConfirmed(false);
        player.setCreated(Instant.now());
        player.setLastSeen(Instant.now());
        return player;
    }

    public Player createBasicPlayer(String playerName, UUID roomPublicId){
        Player player = new Player();
        player.setPublicId(UUID.randomUUID());
        player.setName(playerName);
        player.setRoomPublicId(roomPublicId);
        player.setAdmin(false);
        player.setConnected(true);
        player.setLastSeen(Instant.now());
        return player;
    }
}
