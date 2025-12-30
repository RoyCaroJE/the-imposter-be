package com.roycaroje.theimposter.graphql.room;

import com.roycaroje.theimposter.graphql.player.PlayerDTO;
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
    private List<PlayerDTO> players;
}

