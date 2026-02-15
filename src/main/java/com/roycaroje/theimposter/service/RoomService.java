package com.roycaroje.theimposter.service;

import com.roycaroje.theimposter.graphql.*;
import com.roycaroje.theimposter.graphql.player.*;
import com.roycaroje.theimposter.graphql.room.RoomDTO;
import com.roycaroje.theimposter.model.*;
import com.roycaroje.theimposter.publisher.GameEventPublisher;
import com.roycaroje.theimposter.repository.PlayerRepository;
import com.roycaroje.theimposter.repository.RoomRepository;
import com.roycaroje.theimposter.transformer.RoomTransformer;
import com.roycaroje.theimposter.util.CodeGenerator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@AllArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomTransformer roomTransformer;
    private final PlayerService playerService;
    private final PlayerRepository playerRepository;
    private final GameEventPublisher publisher;
    private final WordService wordService;

    public ResultWithCountFor<RoomDTO> createRoom(int numImpostors, String playerName, Language language, Difficulty difficulty) {
        if(playerName.isBlank()){
            throw new IllegalArgumentException("player_name_cannot_be_empty");
        }

        Room room = roomTransformer.createBaseForNewRoom(numImpostors, language, difficulty);
        room.setCode(setRoomCode());
        room.setStatus(GameStatus.WAITING.toString());
        UUID adminPublicId = playerService.createAdminPlayer(playerName, room.getPublicId());
        room.setAdminPublicId(adminPublicId);

        roomRepository.save(room);

        RoomDTO dto = new RoomDTO();
        roomTransformer.mapModelToDto(room, dto, playerName);

        return new ResultWithCountFor<>(List.of(dto));
    }

    private String setRoomCode(){
        String code;
        do {
            code = CodeGenerator.generateCode();
        } while (roomRepository.findByCode(code).isPresent());
        return code;
    }

    public ResultWithCountFor<RoomDTO> getRoomByCode(String roomCode){
        if(roomCode.isBlank()){
            throw new IllegalArgumentException("Room code cannot be empty");
        }

        Optional<Room> optionalRoom = roomRepository.findByCode(roomCode);
        if(optionalRoom.isEmpty()){
            throw new RuntimeException("Room with this code doesn't exist");
        }
        Room room = optionalRoom.get();
        RoomDTO dto = new RoomDTO();
        dto.setPublicId(room.getPublicId());
        dto.setCode(room.getCode());
        dto.setNumImpostors(room.getNumImpostors());
        dto.setAdminPublicId(room.getAdminPublicId());
        dto.setLanguage(Language.valueOf(room.getLanguage()));
        dto.setGameStatus(GameStatus.valueOf(room.getStatus()));
        dto.setDifficulty(Difficulty.valueOf(room.getDifficulty()));

        List<PlayerDTO> players = playerRepository.findPlayersDTOByRoomPublicId(room.getPublicId());
        dto.setPlayers(players);
        return new ResultWithCountFor<>(List.of(dto));
    }

    @Transactional
    public boolean startGame(UUID roomPublicId){
        Room room = roomRepository.findByPublicId(roomPublicId).orElseThrow(() -> new RuntimeException("Room not found: " + roomPublicId));
        List<Player> players = playerRepository.findByRoomPublicId(roomPublicId);

        checkIfGameCanStart(players, room);

        room.setStatus(GameStatus.STARTED.toString());
        room.setStartedAt(Instant.now());

        String wordForThisRound = wordService.getRandomWord(Language.valueOf(room.getLanguage()), Difficulty.valueOf(room.getDifficulty()));
        List<UUID> imposterPublicIds = pickRandomPlayers(players, room.getNumImpostors());

        for(Player player : players){
            if (imposterPublicIds.contains(player.getPublicId())) {
                player.setImpostor(true);
                player.setConfirmed(false);
                if(room.getLanguage().equals("es")){
                    player.setWord("IMPOSTOR");
                } else {
                    player.setWord("IMPOSTER");
                }
            } else {
                player.setConfirmed(false);
                player.setWord(wordForThisRound);
                player.setImpostor(false);
            }
        }

        room = roomRepository.save(room);
        playerRepository.saveAll(players);

        publisher.publish(new RoomStatusDTO(room.getPublicId(), GameStatus.STARTED, null, Language.valueOf(room.getLanguage()), room.getNumImpostors(), null, null, Difficulty.valueOf(room.getDifficulty()), playerRepository.findPlayersDTOByRoomPublicId(room.getPublicId())));
        return true;
    }

    private void checkIfGameCanStart(List<Player> players, Room room){
        if(players.isEmpty()){
            throw new RuntimeException("No players in the room: " + room.getPublicId());
        }

        boolean allPlayersAreReady = players.stream().allMatch(Player::isConfirmed);
        if(!allPlayersAreReady) {
            throw new IllegalArgumentException("Not all players are ready!");
        }

        int numberOfImposters = room.getNumImpostors();
        if(players.size() < numberOfImposters + 2){
            throw new IllegalArgumentException("This room needs more players to play the game!");
        }
    }

    private static List<UUID> pickRandomPlayers(List<Player> players, int n) {
        if (n > players.size()) {
            throw new IllegalArgumentException("n cannot be larger than the list size");
        }

        List<UUID> copyOfPublicIds = List.copyOf(players.stream().map(Player::getPublicId).toList());
        List<UUID> mutableCopy = new ArrayList<>(copyOfPublicIds);

        Collections.shuffle(mutableCopy);
        return mutableCopy.subList(0, n);
    }

    @Transactional
    public boolean startDiscussion(UUID roomPublicId) {
        Room room = roomRepository.findByPublicId(roomPublicId).orElseThrow(() -> new RuntimeException("Room not found: " + roomPublicId));
        List<Player> players = playerRepository.findByRoomPublicId(roomPublicId);
        boolean allPlayersAreReady = players.stream().allMatch(Player::isConfirmed);
        if (!allPlayersAreReady) {
            throw new IllegalArgumentException("Not all players are ready!");
        }

        for(Player player : players){
            player.setConfirmed(false);
        }
        playerRepository.saveAll(players);

        room.setStatus(GameStatus.PLAYING.toString());
        room.setStartedAt(Instant.now());
        room = roomRepository.save(room);

        List<Player> randomList = new ArrayList<>(List.copyOf(players));
        Collections.shuffle(randomList);
        String startingPlayer = randomList.getFirst().getName();

        publisher.publish(new RoomStatusDTO(room.getPublicId(), GameStatus.PLAYING, startingPlayer, Language.valueOf(room.getLanguage()), room.getNumImpostors(), null, null, Difficulty.valueOf(room.getDifficulty()), playerRepository.findPlayersDTOByRoomPublicId(room.getPublicId())));
        return true;
    }

    @Transactional
    public boolean finishDiscussion(UUID roomPublicId) {
        Room room = roomRepository.findByPublicId(roomPublicId).orElseThrow(() -> new RuntimeException("Room not found: " + roomPublicId));
        List<Player> players = playerRepository.findByRoomPublicId(roomPublicId);
        Optional<Player> firstNonImposter = players.stream().filter(p -> !p.isImpostor()).findFirst();
        if(firstNonImposter.isEmpty()) {
            throw new RuntimeException("Unexpected error: No non-imposter player exists");
        }
        String roomWord = firstNonImposter.get().getWord();
        List<String> imposters = players.stream()
                .filter(Player::isImpostor)
                .map(Player::getName).toList();

        for(Player player : players){
            player.setImpostor(false);
            player.setConfirmed(false);
        }
        playerRepository.saveAll(players);

        room.setStatus(GameStatus.WAITING.toString());
        room.setStartedAt(null);
        room = roomRepository.save(room);

        publisher.publish(new RoomStatusDTO(room.getPublicId(), GameStatus.WAITING, null, Language.valueOf(room.getLanguage()), room.getNumImpostors(), imposters, roomWord, Difficulty.valueOf(room.getDifficulty()), playerRepository.findPlayersDTOByRoomPublicId(room.getPublicId())));
        return true;
    }

    public boolean editRoom(UUID roomPublicId, int numImposters, Language language, Difficulty difficulty){
        Room room = roomRepository.findByPublicId(roomPublicId).orElseThrow(() -> new RuntimeException("Room not found: " + roomPublicId));

        room.setNumImpostors(numImposters);
        room.setLanguage(language.name());
        room.setDifficulty(difficulty.name());
        room = roomRepository.save(room);

        publisher.publish(new RoomStatusDTO(room.getPublicId(), GameStatus.WAITING, null, Language.valueOf(room.getLanguage()), room.getNumImpostors(), null, null, Difficulty.valueOf(room.getDifficulty()), playerRepository.findPlayersDTOByRoomPublicId(room.getPublicId())));
        return true;
    }

}
