package com.roycaroje.theimposter.graphql.room;

import com.roycaroje.theimposter.graphql.player.GameStatus;
import com.roycaroje.theimposter.graphql.player.PlayerDTO;
import com.roycaroje.theimposter.model.Difficulty;
import com.roycaroje.theimposter.model.Language;
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
    private Language language;
    private int numImposters;
    private List<String> lastImposters;
    private String lastWord;
    private Difficulty difficulty;
    private boolean privateRoom;
    private List<PlayerDTO> players;
}

