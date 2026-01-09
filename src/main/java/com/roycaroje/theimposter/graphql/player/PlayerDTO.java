package com.roycaroje.theimposter.graphql.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerDTO {
    private UUID publicId;
    private String name;
    private boolean admin;
    private boolean confirmed;
    private boolean connected;
    private UUID roomPublicId;
}
