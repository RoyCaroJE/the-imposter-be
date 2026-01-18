package com.roycaroje.theimposter.service;

import com.roycaroje.theimposter.graphql.ResultWithCountFor;
import com.roycaroje.theimposter.graphql.player.PlayerDTO;
import com.roycaroje.theimposter.graphql.room.RoomDTO;
import com.roycaroje.theimposter.model.Player;
import com.roycaroje.theimposter.graphql.player.PlayersUpdatedEvent;
import com.roycaroje.theimposter.model.Room;
import com.roycaroje.theimposter.graphql.room.RoomStatus;
import com.roycaroje.theimposter.publisher.GameEventPublisher;
import com.roycaroje.theimposter.repository.PlayerRepository;
import com.roycaroje.theimposter.repository.RoomRepository;
import com.roycaroje.theimposter.transformer.PlayerTransformer;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerTransformer playerTransformer;
    private final RoomRepository roomRepository;
    private final GameEventPublisher publisher;

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    @Scheduled(fixedRate = 5_000)
    // Every 5 seconds we check all connected players and we disconnect them if they're not
    public void updatePlayerPresence() {
        Instant limit = Instant.now().minus(TIMEOUT);

        List<Player> players = playerRepository.findByConnectedTrue();

        for (Player player : players) {
            if (player.getLastSeen() == null || player.getLastSeen().isBefore(limit)) {
                player.setConnected(false);
                playerRepository.save(player);
                publisher.publish(new PlayersUpdatedEvent(player.getRoomPublicId(), playerRepository.findByRoomPublicId(player.getRoomPublicId())));
            }
        }
    }

    public boolean playerHeartbeat(UUID playerPublicId) {
        Player player = playerRepository.findByPublicId(playerPublicId).orElseThrow(() -> new RuntimeException("Player not found"));

        player.setLastSeen(Instant.now());
        player.setConnected(true);

        playerRepository.save(player);
        return true;
    }

    public UUID createAdminPlayer(String playerName, UUID roomPublicId){
        Player player = playerTransformer.createAdminPlayer(playerName, roomPublicId);
        player = playerRepository.save(player);
        return player.getPublicId();
    }

    @Transactional
    public ResultWithCountFor<RoomDTO> joinRoom(String playerName, String roomCode) {
        if(playerName.isBlank() || roomCode.isBlank()){
            throw new IllegalArgumentException("Player name and Room Code cannot be empty");
        }

        Room room = roomRepository.findByCode(roomCode).orElseThrow(() -> new RuntimeException("Room not found"));

        if (!RoomStatus.CREATED.toString().equals(room.getStatus())) {
            throw new RuntimeException("Game already started");
        }

        Player player = playerTransformer.createBasicPlayer(playerName, room.getPublicId());

        player = playerRepository.save(player);
        publisher.publish(new PlayersUpdatedEvent(room.getPublicId(), playerRepository.findByRoomPublicId(room.getPublicId())));

        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setPublicId(room.getPublicId());
        roomDTO.setCode(roomCode);
        roomDTO.setNumImpostors(room.getNumImpostors());
        roomDTO.setAdminPublicId(room.getAdminPublicId());
        roomDTO.setJoinedPlayerPublicId(player.getPublicId());
        List<PlayerDTO> players = playerRepository.findPlayersDTOByRoomPublicId(room.getPublicId());
        roomDTO.setPlayers(players);

        return new ResultWithCountFor<>(List.of(roomDTO));
    }

    public boolean playerReady(UUID playerPublicId) {
        Player player = playerRepository.findByPublicId(playerPublicId).orElseThrow(() -> new RuntimeException("Player Not Found: " + playerPublicId));

        player.setConfirmed(!player.isConfirmed());
        player.setLastSeen(Instant.now());
        player.setConnected(true);
        playerRepository.save(player);

        publisher.publish(new PlayersUpdatedEvent(player.getRoomPublicId(), playerRepository.findByRoomPublicId(player.getRoomPublicId())));

        return true;
    }

    @Transactional
    public boolean leaveRoom(UUID playerPublicId) {
        Player player = playerRepository.findByPublicId(playerPublicId).orElseThrow(() -> new RuntimeException("Player Not Found: " + playerPublicId));
        boolean deletedPlayerWasAdmin = player.isAdmin();
        playerRepository.delete(player);
        List<Player> playersLeft = playerRepository.findByRoomPublicId(player.getRoomPublicId());

        if(playersLeft.isEmpty()){
            Room room = roomRepository.findByPublicId(player.getRoomPublicId()).orElseThrow(() -> new RuntimeException("Error while deleting the room: " + player.getRoomPublicId()));
            roomRepository.delete(room);
        } else {
            if(deletedPlayerWasAdmin) {
                Player randomPlayer = playersLeft.getFirst();
                randomPlayer.setAdmin(true);
                playerRepository.save(randomPlayer);
            }
        }

        publisher.publish(new PlayersUpdatedEvent(player.getRoomPublicId(), playersLeft));

        return true;
    }

}
