package com.roycaroje.theimposter.service;

import com.roycaroje.theimposter.graphql.ResultWithCountFor;
import com.roycaroje.theimposter.graphql.RoomDTO;
import com.roycaroje.theimposter.model.*;
import com.roycaroje.theimposter.publisher.GameEventPublisher;
import com.roycaroje.theimposter.repository.PlayerRepository;
import com.roycaroje.theimposter.repository.RoomRepository;
import com.roycaroje.theimposter.transformer.RoomTransformer;
import com.roycaroje.theimposter.util.CodeGenerator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomTransformer roomTransformer;
    private final PlayerService playerService;
    private final PlayerRepository playerRepository;
    private final GameEventPublisher publisher;

    public ResultWithCountFor<RoomDTO> createRoom(String roomName, int numImpostors, String playerName) {
        Room room = roomTransformer.createBaseForNewRoom(roomName, numImpostors);
        room.setCode(setRoomCode());
        room.setWord(setRoomWord());
        room.setStatus(RoomStatus.CREATED);

        roomRepository.save(room); // SAVE FIRST

        UUID adminPublicId = playerService.createAdminPlayer(playerName, room.getPublicId());
        room.setAdminPublicId(adminPublicId);

        roomRepository.save(room);

        RoomDTO dto = new RoomDTO();
        roomTransformer.mapModelToDto(room, dto);

        return new ResultWithCountFor<>(List.of(dto));
    }


    private String setRoomCode(){
        String code;
        do {
            code = CodeGenerator.generateCode();
        } while (roomRepository.findByCode(code).isPresent());
        return code;
    }

    private String setRoomWord(){
        return "test";
    }

    public void startGame(UUID roomPublicId, UUID adminPublicId) {
        Room room = getRoom(roomPublicId);

        if (!room.getAdminPublicId().equals(adminPublicId)) {
            throw new RuntimeException("Only admin can start game");
        }

        if (!RoomStatus.CREATED.equals(room.getStatus())) {
            throw new RuntimeException("Game already started");
        }

        List<Player> players = playerService.getConfirmedPlayers(roomPublicId);

        if (players.size() <= room.getNumImpostors()) {
            throw new RuntimeException("Not enough players");
        }

        assignImpostors(players, room.getNumImpostors());

        room.setStatus(RoomStatus.STARTED);
        room.setStartedAt(Instant.now());

        roomRepository.save(room);

        for (Player player : players) {
            String word =
                    player.getImpostor() ? "IMPOSTOR" : room.getWord();

            publisher.publish(
                    new GameStartPayload(
                            room.getPublicId(),
                            player.getPublicId(),
                            word
                    )
            );
        }
    }

    private void assignImpostors(List<Player> players, int numImpostors) {
        Collections.shuffle(players);

        for (int i = 0; i < players.size(); i++) {
            players.get(i).setImpostor(i < numImpostors);
        }

        playerRepository.saveAll(players);
    }

    public void endGame(UUID roomPublicId, UUID adminPublicId) {

        Room room = getRoom(roomPublicId);

        if (!room.getAdminPublicId().equals(adminPublicId)) {
            throw new RuntimeException("Only admin can end game");
        }

        if (!RoomStatus.STARTED.equals(room.getStatus())) {
            throw new RuntimeException("Game not started");
        }

        room.setStatus(RoomStatus.ENDED);
        roomRepository.save(room);

        List<Player> players = playerRepository.findByRoomPublicId(roomPublicId);

        List<UUID> impostors = players.stream()
                .filter(Player::getImpostor)
                .map(Player::getPublicId)
                .toList();

        publisher.publish(
                new GameEndPayload(room.getPublicId(), impostors)
        );
    }


    public void restartGame(UUID roomPublicId, UUID adminPublicId) {

        Room room = getRoom(roomPublicId);

        if (!room.getAdminPublicId().equals(adminPublicId)) {
            throw new RuntimeException("Only admin can restart");
        }

        List<Player> players = playerService.getPlayersByRoom(roomPublicId);

        for (Player player : players) {
            player.setImpostor(false);
            player.setConfirmed(false);
        }

        playerRepository.saveAll(players);

        room.setWord(setRoomWord());
        room.setStatus(RoomStatus.CREATED);
        room.setStartedAt(null);

        roomRepository.save(room);

        publisher.publish(
                new GameRestartedEvent(room.getPublicId())
        );
    }

    private Room getRoom(UUID roomPublicId) {
        return roomRepository.findByPublicId(roomPublicId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

}
