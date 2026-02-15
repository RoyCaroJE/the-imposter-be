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
    private String language;
    private int numImposters;
    private List<String> lastImposters;
    private String lastWord;
    private String difficulty;
    private List<PlayerDTO> players;
}

