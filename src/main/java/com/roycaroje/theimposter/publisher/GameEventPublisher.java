package com.roycaroje.theimposter.publisher;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

@Component
public class GameEventPublisher {

    private final Sinks.Many<Object> sink = Sinks.many().replay().limit(1);

    public void publish(Object event) {
        sink.emitNext(event, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    public Flux<Object> flux() {
        return sink.asFlux();
    }
}
