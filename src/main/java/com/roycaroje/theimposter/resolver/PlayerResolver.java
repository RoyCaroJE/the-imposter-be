package com.roycaroje.theimposter.resolver;

import com.roycaroje.theimposter.graphql.ResultWithCountFor;
import com.roycaroje.theimposter.graphql.room.RoomDTO;
import com.roycaroje.theimposter.service.PlayerService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@AllArgsConstructor
public class PlayerResolver {

    private final PlayerService playerService;

    @MutationMapping
    public ResultWithCountFor<RoomDTO> joinRoom(@Argument String roomCode, @Argument String playerName) {
        return playerService.joinRoom(playerName, roomCode);
    }

    @MutationMapping
    public Boolean leaveRoom(@Argument UUID playerPublicId) {
        playerService.leaveRoom(playerPublicId);
        return true;
    }

    @MutationMapping
    public Boolean disconnectPlayer(@Argument UUID playerPublicId) {
        playerService.disconnectPlayer(playerPublicId);
        return true;
    }

    @MutationMapping
    public Boolean reconnectPlayer(@Argument UUID playerPublicId) {
        playerService.reconnectPlayer(playerPublicId);
        return true;
    }

    @MutationMapping
    public Boolean playerReady(@Argument UUID playerPublicId) {
        playerService.playerReady(playerPublicId);
        return true;
    }

    @MutationMapping
    public boolean playerHeartbeat(@Argument UUID playerPublicId) {
        return playerService.playerHeartbeat(playerPublicId);
    }
}

