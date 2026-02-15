package com.roycaroje.theimposter.service;

import com.roycaroje.theimposter.graphql.ResultWithCountFor;
import com.roycaroje.theimposter.graphql.player.GameStatus;
import com.roycaroje.theimposter.graphql.player.PlayerDTO;
import com.roycaroje.theimposter.graphql.player.PlayerWordDTO;
import com.roycaroje.theimposter.graphql.room.RoomDTO;
import com.roycaroje.theimposter.model.Difficulty;
import com.roycaroje.theimposter.model.Language;
import com.roycaroje.theimposter.model.Player;
import com.roycaroje.theimposter.graphql.player.RoomStatusDTO;
import com.roycaroje.theimposter.model.Room;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerTransformer playerTransformer;
    private final RoomRepository roomRepository;
    private final GameEventPublisher publisher;

    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    @Scheduled(fixedRate = 5_000)
    // Every 5 seconds we check all connected players and we disconnect them if they're not
    public void updatePlayerPresence() {
        Instant limit = Instant.now().minus(TIMEOUT);

        List<Player> players = playerRepository.findByConnectedTrue();

        for (Player player : players) {
            Room room = roomRepository.findByPublicId(player.getRoomPublicId()).orElseThrow();
            if (player.getLastSeen() == null || player.getLastSeen().isBefore(limit)) {
                player.setConnected(false);
                playerRepository.save(player);
                if(GameStatus.WAITING.toString().equals(room.getStatus())){
                    publisher.publish(new RoomStatusDTO(player.getRoomPublicId(), GameStatus.valueOf(room.getStatus()), null, Language.valueOf(room.getLanguage()), room.getNumImpostors(), null, null, Difficulty.valueOf(room.getDifficulty()), playerRepository.findPlayersDTOByRoomPublicId(player.getRoomPublicId())));
                }
            }
        }
    }

    @Transactional
    @Scheduled(fixedRate = 5 * 60 * 1_000) // Every 5 minutes we check if there are empty rooms to delete
    public void cleanEmptyRooms() {

        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);

        List<Room> rooms = roomRepository.findByCreatedBefore(oneHourAgo);
        if (rooms.isEmpty()) {
            return;
        }

        List<UUID> roomPublicIds = rooms.stream()
                .map(Room::getPublicId)
                .toList();

        List<Player> players = playerRepository.findByRoomPublicIdIn(roomPublicIds);

        Map<UUID, List<Player>> playersByRoom = players.stream()
                .collect(Collectors.groupingBy(Player::getRoomPublicId));

        List<Room> roomsToDelete = rooms.stream()
                .filter(room -> {
                    List<Player> roomPlayers =
                            playersByRoom.getOrDefault(room.getPublicId(), List.of());

                    if (roomPlayers.isEmpty()) {
                        return true;
                    }

                    return roomPlayers.stream().allMatch(player ->
                            !player.isConnected()
                                    && player.getLastSeen() != null
                                    && player.getLastSeen().isBefore(oneHourAgo)
                    );
                })
                .toList();

        if (roomsToDelete.isEmpty()) {
            return;
        }

        List<UUID> roomsToDeleteIds = roomsToDelete.stream()
                .map(Room::getPublicId)
                .toList();

        List<Player> playersToDelete = players.stream()
                .filter(p -> roomsToDeleteIds.contains(p.getRoomPublicId()))
                .toList();

        if (!playersToDelete.isEmpty()) {
            playerRepository.deleteAll(playersToDelete);
        }

        roomRepository.deleteAll(roomsToDelete);
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
            throw new IllegalArgumentException("player_and_room_cannot_be_empty");
        }

        Room room = roomRepository.findByCode(roomCode).orElseThrow(() -> new RuntimeException("room_not_found"));
        List<PlayerDTO> playersBefore = playerRepository.findPlayersDTOByRoomPublicId(room.getPublicId());
        boolean playerNameAlreadyExists = playersBefore.stream().anyMatch(p -> p.getName().equalsIgnoreCase(playerName));
        if(playerNameAlreadyExists) {
            throw new IllegalArgumentException("player_name_already_exists");
        }

        if (!GameStatus.WAITING.toString().equals(room.getStatus())) {
            throw new RuntimeException("game_already_started");
        }

        Player player = playerTransformer.createBasicPlayer(playerName, room.getPublicId());

        player = playerRepository.save(player);

        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setPublicId(room.getPublicId());
        roomDTO.setCode(roomCode);
        roomDTO.setNumImpostors(room.getNumImpostors());
        roomDTO.setAdminPublicId(room.getAdminPublicId());
        roomDTO.setJoinedPlayerPublicId(player.getPublicId());
        roomDTO.setGameStatus(GameStatus.valueOf(room.getStatus()));
        List<PlayerDTO> players = playerRepository.findPlayersDTOByRoomPublicId(room.getPublicId());
        roomDTO.setPlayers(players);
        publisher.publish(new RoomStatusDTO(room.getPublicId(), GameStatus.WAITING, null, Language.valueOf(room.getLanguage()), room.getNumImpostors(), null, null, Difficulty.valueOf(room.getDifficulty()), playerRepository.findPlayersDTOByRoomPublicId(room.getPublicId())));

        return new ResultWithCountFor<>(List.of(roomDTO));
    }

    public boolean playerReady(UUID playerPublicId) {
        Player player = playerRepository.findByPublicId(playerPublicId).orElseThrow(() -> new RuntimeException("Player Not Found: " + playerPublicId));
        Room room = roomRepository.findByPublicId(player.getRoomPublicId()).orElseThrow(() -> new RuntimeException("Room Not Found: " + player.getRoomPublicId()));

        player.setConfirmed(!player.isConfirmed());
        player.setLastSeen(Instant.now());
        player.setConnected(true);
        playerRepository.save(player);

        publisher.publish(new RoomStatusDTO(player.getRoomPublicId(), GameStatus.valueOf(room.getStatus()), null, Language.valueOf(room.getLanguage()), room.getNumImpostors(), null, null, Difficulty.valueOf(room.getDifficulty()), playerRepository.findPlayersDTOByRoomPublicId(player.getRoomPublicId())));

        return true;
    }

    @Transactional
    public boolean leaveRoom(UUID playerPublicId) {
        Player player = playerRepository.findByPublicId(playerPublicId).orElseThrow(() -> new RuntimeException("Player Not Found: " + playerPublicId));
        Room room = roomRepository.findByPublicId(player.getRoomPublicId()).orElseThrow(() -> new RuntimeException("Room Not Found: " + player.getRoomPublicId()));

        boolean deletedPlayerWasAdmin = player.isAdmin();
        playerRepository.delete(player);
        List<PlayerDTO> playersLeft = playerRepository.findPlayersDTOByRoomPublicId(player.getRoomPublicId());

        if(playersLeft.isEmpty()){
            roomRepository.delete(room);
        } else {
            if(deletedPlayerWasAdmin) {
                Player randomPlayer = playerRepository.findByPublicId(playersLeft.getFirst().getPublicId()).orElseThrow(() -> new RuntimeException("Player not found"));
                randomPlayer.setAdmin(true);
                playersLeft.getFirst().setAdmin(true);
                playerRepository.save(randomPlayer);
            }
        }

        publisher.publish(new RoomStatusDTO(player.getRoomPublicId(), GameStatus.WAITING, null, Language.valueOf(room.getLanguage()), room.getNumImpostors(), null, null, Difficulty.valueOf(room.getDifficulty()), playersLeft));

        return true;
    }

    @Transactional
    public boolean kickPlayer(UUID playerPublicId) {
        Player player = playerRepository.findByPublicId(playerPublicId).orElseThrow(() -> new RuntimeException("Player Not Found: " + playerPublicId));
        Room room = roomRepository.findByPublicId(player.getRoomPublicId()).orElseThrow(() -> new RuntimeException("Room Not Found: " + player.getRoomPublicId()));

        boolean deletedPlayerIsAdmin = player.isAdmin();
        if(deletedPlayerIsAdmin){
            throw new RuntimeException("Admin cannot be kicked!");
        }
        playerRepository.delete(player);
        List<PlayerDTO> playersLeft = playerRepository.findPlayersDTOByRoomPublicId(player.getRoomPublicId());

        publisher.publish(new RoomStatusDTO(player.getRoomPublicId(), GameStatus.WAITING, null, Language.valueOf(room.getLanguage()), room.getNumImpostors(), null, null, Difficulty.valueOf(room.getDifficulty()), playersLeft));

        return true;
    }

    public ResultWithCountFor<PlayerWordDTO> getPlayerWord(UUID playerPublicId){
        String word = playerRepository.findWordByPlayerPublicId(playerPublicId);

        if(word == null || word.isBlank()){
            throw new RuntimeException("Error fetching the word for player: " + playerPublicId);
        }

        PlayerWordDTO dto = new PlayerWordDTO(word);

        return new ResultWithCountFor<>(List.of(dto));
    }

}
