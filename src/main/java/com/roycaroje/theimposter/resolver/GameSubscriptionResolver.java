package com.roycaroje.theimposter.resolver;

import com.roycaroje.theimposter.graphql.room.RoomStatusDTO;
import com.roycaroje.theimposter.publisher.GameEventPublisher;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Controller
@AllArgsConstructor
public class GameSubscriptionResolver {

    private final GameEventPublisher publisher;

    @SubscriptionMapping
    public Flux<RoomStatusDTO> roomStatus(@Argument UUID roomPublicId) {
        return publisher.flux()
                .ofType(RoomStatusDTO.class)
                .filter(e -> e.getRoomPublicId().equals(roomPublicId));
    }
}

