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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    private static final HttpClient client = HttpClient.newHttpClient();

    public ResultWithCountFor<RoomDTO> createRoom(int numImpostors, String playerName, String language) {
        if(playerName.isBlank()){
            throw new IllegalArgumentException("Player name cannot be empty");
        }
        if(!(language.equals("es") || language.equals("en"))){
            throw new IllegalArgumentException("Incorrect language: " + language);
        }

        Room room = roomTransformer.createBaseForNewRoom(numImpostors, language);
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

    private String setRoomWord(String language) {
        try {
            String url = "https://random-word-api.herokuapp.com/word?lang=" + language;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            return body.replace("[\"", "").replace("\"]", "");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
        List<PlayerDTO> players = playerRepository.findPlayersDTOByRoomPublicId(room.getPublicId());
        dto.setPlayers(players);
        return new ResultWithCountFor<>(List.of(dto));
    }

    @Transactional
    public boolean startGame(UUID roomPublicId){
        Room room = roomRepository.findByPublicId(roomPublicId).orElseThrow(() -> new RuntimeException("Room not found: " + roomPublicId));
        List<Player> players = playerRepository.findByRoomPublicId(roomPublicId);
        boolean allPlayersAreReady = players.stream().allMatch(Player::isConfirmed);
        if(!allPlayersAreReady) {
            throw new IllegalArgumentException("Not all players are ready!");
        }
        int numberOfImposters = room.getNumImpostors();
        if(numberOfImposters + 2 < players.size()){
            throw new IllegalArgumentException("This room needs more players to play the game!");
        }
        room.setStatus(GameStatus.STARTED.toString());
        room.setStartedAt(Instant.now());
        room = roomRepository.save(room);

        String wordForThisRound = setRoomWord(room.getLanguage());
        List<Player> imposters = pickRandomPlayers(players, numberOfImposters);

        for(Player player : players){
            if (imposters.contains(player)) {
                player.setImpostor(true);
                player.setConfirmed(false);
                player.setWord("IMPOSTER");
            } else {
                player.setConfirmed(false);
                player.setWord(wordForThisRound);
                player.setImpostor(false);
            }
        }

        playerRepository.saveAll(players);

        publisher.publish(new RoomStatusDTO(room.getPublicId(), GameStatus.STARTED, null, playerRepository.findPlayersDTOByRoomPublicId(room.getPublicId())));
        return true;
    }

    private static List<Player> pickRandomPlayers(List<Player> players, int n) {
        if (n > players.size()) {
            throw new IllegalArgumentException("n cannot be larger than the list size");
        }

        List<Player> copy = List.copyOf(players);
        List<Player> mutableCopy = new ArrayList<>(copy);

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

        publisher.publish(new RoomStatusDTO(room.getPublicId(), GameStatus.PLAYING, startingPlayer, playerRepository.findPlayersDTOByRoomPublicId(room.getPublicId())));
        return true;
    }

    @Transactional
    public boolean finishDiscussion(UUID roomPublicId) {
        Room room = roomRepository.findByPublicId(roomPublicId).orElseThrow(() -> new RuntimeException("Room not found: " + roomPublicId));
        List<Player> players = playerRepository.findByRoomPublicId(roomPublicId);

        for(Player player : players){
            player.setImpostor(false);
            player.setConfirmed(false);
        }
        playerRepository.saveAll(players);

        room.setStatus(GameStatus.WAITING.toString());
        room.setStartedAt(null);
        room = roomRepository.save(room);

        publisher.publish(new RoomStatusDTO(room.getPublicId(), GameStatus.WAITING, null, playerRepository.findPlayersDTOByRoomPublicId(room.getPublicId())));
        return true;
    }

}
