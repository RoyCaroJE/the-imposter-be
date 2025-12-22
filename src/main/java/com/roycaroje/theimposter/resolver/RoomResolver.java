package com.roycaroje.theimposter.resolver;

import com.roycaroje.theimposter.graphql.ResultWithCountFor;
import com.roycaroje.theimposter.graphql.RoomDTO;
import com.roycaroje.theimposter.service.RoomService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class RoomResolver {

    private final RoomService roomService;

    @MutationMapping
    public ResultWithCountFor<RoomDTO> createRoom(@Argument String roomName, @Argument int numImpostors, @Argument String playerName) {
        return roomService.createRoom(roomName, numImpostors, playerName);
    }
}
