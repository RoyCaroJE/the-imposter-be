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

    @MutationMapping
    public ResultWithCountFor<RoomDTO> createRoom(@Argument int numImpostors, @Argument String playerName) {
        return roomService.createRoom(numImpostors, playerName);
    }

    @MutationMapping
    public Boolean startGame(
            @Argument UUID roomPublicId,
            @Argument UUID adminPublicId) {
        roomService.startGame(roomPublicId, adminPublicId);
        return true;
    }

    @MutationMapping
    public Boolean startDiscussion(
            @Argument UUID roomPublicId,
            @Argument UUID adminPublicId) {
        roomService.startDiscussion(roomPublicId, adminPublicId);
        return true;
    }

    @MutationMapping
    public Boolean endGame(
            @Argument UUID roomPublicId,
            @Argument UUID adminPublicId) {
        roomService.endGame(roomPublicId, adminPublicId);
        return true;
    }

    @MutationMapping
    public Boolean restartGame(
            @Argument UUID roomPublicId,
            @Argument UUID adminPublicId) {
        roomService.restartGame(roomPublicId, adminPublicId);
        return true;
    }

    @QueryMapping
    public ResultWithCountFor<RoomDTO> getRoomByCode(@Argument String roomCode){
        return roomService.getRoomByCode(roomCode);
    }
}

