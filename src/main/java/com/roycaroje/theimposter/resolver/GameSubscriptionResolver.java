package com.roycaroje.theimposter.resolver;

import com.roycaroje.theimposter.graphql.player.*;
import com.roycaroje.theimposter.model.*;
import com.roycaroje.theimposter.publisher.GameEventPublisher;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Controller
@AllArgsConstructor
public class GameSubscriptionResolver {

    private final GameEventPublisher publisher;

    @SubscriptionMapping
    public Flux<GameStartPayload> gameStarted(
            @Argument UUID roomPublicId,
            @Argument UUID playerPublicId) {

        return publisher.flux()
                .ofType(GameStartPayload.class)
                .filter(e ->
                        e.getRoomPublicId().equals(roomPublicId) &&
                                e.getPlayerPublicId().equals(playerPublicId)
                );
    }

    @SubscriptionMapping
    public Flux<Boolean> allPlayersReady(@Argument UUID roomPublicId) {
        return publisher.flux()
                .ofType(Boolean.class);
    }

    @SubscriptionMapping
    public Flux<DiscussionStartedPayload> discussionStarted(
            @Argument UUID roomPublicId) {

        return publisher.flux()
                .ofType(DiscussionStartedPayload.class)
                .filter(e -> e.getRoomPublicId().equals(roomPublicId));
    }

    @SubscriptionMapping
    public Flux<GameEndPayload> gameEnded(
            @Argument UUID roomPublicId) {

        return publisher.flux()
                .ofType(GameEndPayload.class)
                .filter(e -> e.getRoomPublicId().equals(roomPublicId));
    }

    @SubscriptionMapping
    public Flux<List<Player>> playersUpdated(@Argument UUID roomPublicId) {
        return publisher.flux()
                .ofType(PlayersUpdatedEvent.class)
                .filter(e -> e.getRoomPublicId().equals(roomPublicId))
                .map(PlayersUpdatedEvent::getPlayers);
    }

    @SubscriptionMapping
    public Flux<Boolean> gameRestarted(
            @Argument UUID roomPublicId) {

        return publisher.flux()
                .ofType(GameRestartedEvent.class)
                .filter(e -> e.getRoomPublicId().equals(roomPublicId))
                .map(e -> true);
    }
}

