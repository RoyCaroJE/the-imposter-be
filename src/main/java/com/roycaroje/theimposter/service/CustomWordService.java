package com.roycaroje.theimposter.service;

import com.roycaroje.theimposter.graphql.player.GameStatus;
import com.roycaroje.theimposter.graphql.room.RoomStatusDTO;
import com.roycaroje.theimposter.model.*;
import com.roycaroje.theimposter.publisher.GameEventPublisher;
import com.roycaroje.theimposter.repository.CustomWordRepository;
import com.roycaroje.theimposter.repository.PlayerRepository;
import com.roycaroje.theimposter.repository.RoomRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CustomWordService {

    private final CustomWordRepository customWordRepository;
    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;
    private final GameEventPublisher publisher;

    @Transactional
    public boolean addWords(UUID playerPublicId, UUID roomPublicId, List<String> words) {
        Room room = roomRepository.findByPublicId(roomPublicId).orElseThrow(() -> new RuntimeException("Room not found: " + roomPublicId));
        Player player = playerRepository.findByPublicId(playerPublicId).orElseThrow(() -> new RuntimeException("Player not found: " + playerPublicId));

        validateAddWordsRequest(room, words);

        List<CustomWord> customWords = new ArrayList<>();
        for (String word : words) {
            CustomWord customWord = getCustomWord(playerPublicId, roomPublicId, word);
            customWords.add(customWord);
        }
        customWordRepository.saveAll(customWords);

        player.setConfirmed(true);
        player.setLastSeen(Instant.now());
        playerRepository.save(player);

        publisher.publish(new RoomStatusDTO(room.getPublicId(), GameStatus.valueOf(room.getStatus()), null, Language.valueOf(room.getLanguage()), room.getNumImpostors(), null, null, Difficulty.valueOf(room.getDifficulty()), room.isPrivateRoom(), playerRepository.findPlayersDTOByRoomPublicId(room.getPublicId())));

        return true;
    }

    private static void validateAddWordsRequest(Room room, List<String> words) {
        if (!Difficulty.CUSTOM.name().equals(room.getDifficulty())) {
            throw new IllegalArgumentException("room_difficulty_must_be_custom");
        }

        if (words == null || words.isEmpty()) {
            throw new IllegalArgumentException("words_cannot_be_empty");
        }
    }

    private static CustomWord getCustomWord(UUID playerPublicId, UUID roomPublicId, String word) {
        String trimmedWord = word != null ? word.trim() : null;
        if (trimmedWord == null || trimmedWord.isBlank()) {
            throw new IllegalArgumentException("words_cannot_be_blank");
        }
        if (trimmedWord.length() > 50) {
            throw new IllegalArgumentException("word_length_exceeds_limit");
        }
        CustomWord customWord = new CustomWord();
        customWord.setPlayerPublicId(playerPublicId);
        customWord.setRoomPublicId(roomPublicId);
        customWord.setWord(trimmedWord);
        customWord.setPicked(false);
        return customWord;
    }

    @Transactional
    public boolean startWordSubmission(UUID roomPublicId) {
        Room room = roomRepository.findByPublicId(roomPublicId).orElseThrow(() -> new RuntimeException("Room not found: " + roomPublicId));
        List<Player> players = playerRepository.findByRoomPublicId(roomPublicId);

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

        if (!Difficulty.CUSTOM.name().equals(room.getDifficulty())) {
            throw new IllegalArgumentException("room_difficulty_must_be_custom");
        }

        players.forEach(player -> {
            player.setConfirmed(false);
            player.setLastSeen(Instant.now());
        });
        playerRepository.saveAll(players);

        room.setStatus(GameStatus.WORD_SUBMISSION.name());
        roomRepository.save(room);

        publisher.publish(new RoomStatusDTO(room.getPublicId(), GameStatus.WORD_SUBMISSION, null, Language.valueOf(room.getLanguage()), room.getNumImpostors(), null, null, Difficulty.valueOf(room.getDifficulty()), room.isPrivateRoom(), playerRepository.findPlayersDTOByRoomPublicId(room.getPublicId())));

        return true;
    }
}

