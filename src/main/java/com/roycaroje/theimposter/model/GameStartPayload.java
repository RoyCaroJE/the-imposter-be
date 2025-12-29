package com.roycaroje.theimposter.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class GameStartPayload {
    private UUID roomPublicId;
    private UUID playerPublicId;
    private String word;
}

