package com.roycaroje.theimposter.resolver;

import com.roycaroje.theimposter.graphql.ResultWithCountFor;
import com.roycaroje.theimposter.graphql.player.PlayerWordDTO;
import com.roycaroje.theimposter.graphql.room.RoomDTO;
import com.roycaroje.theimposter.service.PlayerService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@AllArgsConstructor
public class PlayerResolver {

    private final PlayerService playerService;

    // MUTATIONS

    @MutationMapping
    public ResultWithCountFor<RoomDTO> joinRoom(@Argument String roomCode, @Argument String playerName) {
        return playerService.joinRoom(playerName, roomCode);
    }

    @MutationMapping
    public boolean playerHeartbeat(@Argument UUID playerPublicId) {
        return playerService.playerHeartbeat(playerPublicId);
    }

    @MutationMapping
    public boolean playerReady(@Argument UUID playerPublicId) {
        return playerService.playerReady(playerPublicId);
    }

    @MutationMapping
    public boolean leaveRoom(@Argument UUID playerPublicId) {
        return playerService.leaveRoom(playerPublicId);
    }

    @MutationMapping
    public boolean kickPlayer(@Argument UUID playerPublicId) {
        return playerService.kickPlayer(playerPublicId);
    }

    // QUERIES
    @QueryMapping
    public ResultWithCountFor<PlayerWordDTO> getPlayerWord(@Argument UUID playerPublicId) {
        return playerService.getPlayerWord(playerPublicId);
    }
}

