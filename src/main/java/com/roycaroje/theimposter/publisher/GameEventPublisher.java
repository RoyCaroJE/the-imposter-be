package com.roycaroje.theimposter.publisher;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class GameEventPublisher {

    private final Sinks.Many<Object> sink =
            Sinks.many().multicast().onBackpressureBuffer();

    public void publish(Object event) {
        sink.tryEmitNext(event);
    }

    public Flux<Object> flux() {
        return sink.asFlux();
    }
}
