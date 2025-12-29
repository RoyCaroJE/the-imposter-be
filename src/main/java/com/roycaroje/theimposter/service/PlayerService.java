package com.roycaroje.theimposter.service;

import com.roycaroje.theimposter.model.Player;
import com.roycaroje.theimposter.model.PlayersUpdatedEvent;
import com.roycaroje.theimposter.model.Room;
import com.roycaroje.theimposter.model.RoomStatus;
import com.roycaroje.theimposter.publisher.GameEventPublisher;
import com.roycaroje.theimposter.repository.PlayerRepository;
import com.roycaroje.theimposter.repository.RoomRepository;
import com.roycaroje.theimposter.transformer.PlayerTransformer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerTransformer playerTransformer;
    private final RoomRepository roomRepository;
    private final GameEventPublisher publisher;

    public UUID createAdminPlayer(String playerName, UUID roomPublicId){
        Player player = playerTransformer.createAdminPlayer(playerName, roomPublicId);
        player = playerRepository.save(player);
        return player.getPublicId();
    }

    public UUID joinRoom(String playerName, String roomCode) {

        Room room = roomRepository.findByCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!RoomStatus.CREATED.equals(room.getStatus())) {
            throw new RuntimeException("Game already started");
        }

        Player player = new Player();
        player.setName(playerName);
        player.setRoomPublicId(room.getPublicId());
        player.setAdmin(false);
        player.setConnected(true);

        playerRepository.save(player);
        publisher.publish(
                new PlayersUpdatedEvent(
                        room.getPublicId(),
                        playerRepository.findByRoomPublicId(room.getPublicId())
                )
        );

        return player.getPublicId();
    }

    public void leaveRoom(UUID playerPublicId) {
        Player player = getPlayer(playerPublicId);

        if (player.getAdmin()) {
            throw new RuntimeException("Admin cannot leave room");
        }

        playerRepository.delete(player);
        publisher.publish(
                new PlayersUpdatedEvent(
                        player.getRoomPublicId(),
                        playerRepository.findByRoomPublicId(player.getRoomPublicId())
                )
        );

    }

    public void disconnectPlayer(UUID playerPublicId) {

        Player player = getPlayer(playerPublicId);
        player.setConnected(false);

        playerRepository.save(player);

        publisher.publish(
                new PlayersUpdatedEvent(
                        player.getRoomPublicId(),
                        playerRepository.findByRoomPublicId(player.getRoomPublicId())
                )
        );

    }

    public void reconnectPlayer(UUID playerPublicId) {

        Player player = getPlayer(playerPublicId);
        player.setConnected(true);

        playerRepository.save(player);

        publisher.publish(
                new PlayersUpdatedEvent(
                        player.getRoomPublicId(),
                        playerRepository.findByRoomPublicId(player.getRoomPublicId())
                )
        );

    }

    private Player getPlayer(UUID playerPublicId) {
        return playerRepository.findByPublicId(playerPublicId)
                .orElseThrow(() -> new RuntimeException("Player not found"));
    }

    public List<Player> getPlayersByRoom(UUID roomPublicId) {
        return playerRepository.findByRoomPublicId(roomPublicId);
    }

    public List<Player> getConfirmedPlayers(UUID roomPublicId) {
        return playerRepository.findByRoomPublicIdAndConfirmedTrue(roomPublicId);
    }

}
