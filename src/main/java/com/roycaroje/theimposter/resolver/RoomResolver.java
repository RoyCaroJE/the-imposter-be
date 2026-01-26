package com.roycaroje.theimposter.resolver;

import com.roycaroje.theimposter.graphql.ResultWithCountFor;
import com.roycaroje.theimposter.graphql.room.RoomDTO;
import com.roycaroje.theimposter.service.RoomService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@AllArgsConstructor
public class RoomResolver {

    private final RoomService roomService;

    // MUTATIONS

    @MutationMapping
    public ResultWithCountFor<RoomDTO> createRoom(@Argument int numImpostors, @Argument String playerName, @Argument String language) {
        return roomService.createRoom(numImpostors, playerName, language);
    }

    @MutationMapping
    public boolean startGame(@Argument UUID roomPublicId) {
        return roomService.startGame(roomPublicId);
    }

    @MutationMapping
    public boolean startDiscussion(@Argument UUID roomPublicId) {
        return roomService.startDiscussion(roomPublicId);
    }

    @MutationMapping
    public boolean finishDiscussion(@Argument UUID roomPublicId) {
        return roomService.finishDiscussion(roomPublicId);
    }

    @MutationMapping
    public boolean editRoom(@Argument UUID roomPublicId, @Argument int numImposters, @Argument String language){
        return roomService.editRoom(roomPublicId, numImposters, language);
    }

    // QUERIES

    @QueryMapping
    public ResultWithCountFor<RoomDTO> getRoomByCode(@Argument String roomCode){
        return roomService.getRoomByCode(roomCode);
    }
}

