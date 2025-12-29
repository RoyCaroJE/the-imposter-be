package com.roycaroje.theimposter.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor @Getter
public class DiscussionStartedPayload {
    private UUID roomPublicId;
    private UUID startingPlayerPublicId;
}
