package com.roycaroje.theimposter.graphql.room;

import com.roycaroje.theimposter.graphql.player.GameStatus;
import com.roycaroje.theimposter.graphql.player.PlayerDTO;
import com.roycaroje.theimposter.model.Difficulty;
import com.roycaroje.theimposter.model.Language;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RoomDTO {
    private UUID publicId;
    private String code;
    private UUID adminPublicId;
    private int numImpostors;
    private Language language;
    private UUID joinedPlayerPublicId;
    private GameStatus gameStatus;
    private Difficulty difficulty;
    private List<PlayerDTO> players;
}

