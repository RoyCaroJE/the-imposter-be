package com.roycaroje.theimposter.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class GameEndPayload {
    private UUID roomPublicId;
    private List<UUID> impostors;
}

