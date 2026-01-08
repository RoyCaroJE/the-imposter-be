package com.roycaroje.theimposter.transformer;

import com.roycaroje.theimposter.graphql.player.PlayerDTO;
import com.roycaroje.theimposter.graphql.room.RoomDTO;
import com.roycaroje.theimposter.graphql.room.RoomStatus;
import com.roycaroje.theimposter.model.Room;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class RoomTransformer {

    public Room createBaseForNewRoom(int numImpostors){
        Room room = new Room();
        room.setPublicId(UUID.randomUUID());
        room.setStatus(RoomStatus.CREATED.toString());
        room.setNumImpostors(numImpostors);
        room.setCreated(Instant.now());
        return room;
    }

    public void mapModelToDto(Room model, RoomDTO dto, String playerName){
        dto.setCode(model.getCode());
        dto.setAdminPublicId(model.getAdminPublicId());
        dto.setPublicId(model.getPublicId());
        dto.setNumImpostors(model.getNumImpostors());
        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setPublicId(model.getAdminPublicId());
        playerDTO.setName(playerName);
        dto.setPlayers(List.of(playerDTO));
        dto.setJoinedPlayerPublicId(model.getAdminPublicId());
    }
}
