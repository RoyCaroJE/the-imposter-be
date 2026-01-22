package com.roycaroje.theimposter.graphql.player;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class RoomStatusDTO {
    private UUID roomPublicId;
    private GameStatus status;
    private String startingPlayer;
    private List<PlayerDTO> players;
}

