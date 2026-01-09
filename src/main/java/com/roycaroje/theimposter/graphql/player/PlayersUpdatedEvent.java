package com.roycaroje.theimposter.graphql.player;

import com.roycaroje.theimposter.model.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PlayersUpdatedEvent {
    private UUID roomPublicId;
    private List<Player> players;
}
