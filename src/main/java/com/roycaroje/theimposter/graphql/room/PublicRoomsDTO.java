package com.roycaroje.theimposter.graphql.room;

import com.roycaroje.theimposter.model.Difficulty;
import com.roycaroje.theimposter.model.Language;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PublicRoomsDTO {
    private final UUID publicId;
    private final String code;
    private final String adminName;
    private final int numImpostors;
    private final Language language;
    private final Difficulty difficulty;

    public PublicRoomsDTO(UUID publicId, String code, String adminName, int numImpostors, String language, String difficulty) {
        this.publicId = publicId;
        this.code = code;
        this.adminName = adminName;
        this.numImpostors = numImpostors;
        this.language = Language.valueOf(language);
        this.difficulty = Difficulty.valueOf(difficulty);
    }
}
