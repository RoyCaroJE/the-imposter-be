package com.roycaroje.theimposter.model;

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
