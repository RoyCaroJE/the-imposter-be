package com.roycaroje.theimposter.service;

import com.roycaroje.theimposter.graphql.*;
import com.roycaroje.theimposter.graphql.player.*;
import com.roycaroje.theimposter.graphql.room.RoomDTO;
import com.roycaroje.theimposter.graphql.room.RoomStatus;
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
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomTransformer roomTransformer;
    private final PlayerService playerService;
    private final PlayerRepository playerRepository;
    private final GameEventPublisher publisher;

    public ResultWithCountFor<RoomDTO> createRoom(int numImpostors, String playerName) {
        if(playerName.isBlank()){
            throw new IllegalArgumentException("Player name cannot be empty");
        }

        Room room = roomTransformer.createBaseForNewRoom(numImpostors);
        room.setCode(setRoomCode());
        room.setWord(setRoomWord()); // TODO Implement different words and languages
        room.setStatus(RoomStatus.CREATED.toString());
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

    private String setRoomWord(){
        return "test"; // TODO Implement words logic
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

}
