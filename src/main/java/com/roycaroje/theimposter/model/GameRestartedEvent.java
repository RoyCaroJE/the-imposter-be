package com.roycaroje.theimposter.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class GameRestartedEvent {
    private UUID roomPublicId;
}
