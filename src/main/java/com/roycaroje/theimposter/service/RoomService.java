package com.roycaroje.theimposter.service;

import com.roycaroje.theimposter.graphql.ResultWithCountFor;
import com.roycaroje.theimposter.graphql.RoomDTO;
import com.roycaroje.theimposter.model.Room;
import com.roycaroje.theimposter.model.RoomStatus;
import com.roycaroje.theimposter.repository.RoomRepository;
import com.roycaroje.theimposter.transformer.RoomTransformer;
import com.roycaroje.theimposter.util.CodeGenerator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomTransformer roomTransformer;
    private final PlayerService playerService;

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
}
