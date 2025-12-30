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

    public ResultWithCountFor<RoomDTO> joinRoom(String playerName, String roomCode) {
        if(playerName.isBlank() || roomCode.isBlank()){
            throw new IllegalArgumentException("Player name and Room Code cannot be empty");
        }

        Room room = roomRepository.findByCode(roomCode).orElseThrow(() -> new RuntimeException("Room not found"));

        if (!RoomStatus.CREATED.toString().equals(room.getStatus())) {
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

        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setPublicId(room.getPublicId());
        roomDTO.setCode(roomCode);
        roomDTO.setNumImpostors(room.getNumImpostors());
        roomDTO.setAdminPublicId(room.getAdminPublicId());
        List<PlayerDTO> players = playerRepository.findPlayersDTOByRoomPublicId(room.getPublicId());
        roomDTO.setPlayers(players);

        return new ResultWithCountFor<>(List.of(roomDTO));
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

    public void playerReady(UUID playerPublicId) {

        Player player = getPlayer(playerPublicId);
        player.setConfirmed(true);
        playerRepository.save(player);

        List<Player> players =
                playerRepository.findByRoomPublicId(player.getRoomPublicId());

        publisher.publish(
                new PlayersUpdatedEvent(player.getRoomPublicId(), players)
        );

        boolean allReady = players.stream()
                .allMatch(Player::getConfirmed);

        if (allReady) {
            publisher.publish(true);
        }
    }


}
