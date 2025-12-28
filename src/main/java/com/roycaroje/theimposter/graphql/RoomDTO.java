package com.roycaroje.theimposter.graphql;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RoomDTO {
    private UUID publicId;
    private String code;
    private String status;
    private UUID adminPublicId;
    private int numImpostors;
    private String word;
    private String created;
}
