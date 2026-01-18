package com.roycaroje.theimposter.resolver;

import com.roycaroje.theimposter.graphql.player.*;
import com.roycaroje.theimposter.model.*;
import com.roycaroje.theimposter.publisher.GameEventPublisher;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.util.concurrent.Queues;

import java.util.List;
import java.util.UUID;

@Controller
@AllArgsConstructor
public class GameSubscriptionResolver {

    private final GameEventPublisher publisher;

    @SubscriptionMapping
    public Flux<List<Player>> playersUpdated(@Argument UUID roomPublicId) {
        return publisher.flux()
                .ofType(PlayersUpdatedEvent.class)
                .filter(e -> e.getRoomPublicId().equals(roomPublicId))
                .map(PlayersUpdatedEvent::getPlayers);
    }
}

