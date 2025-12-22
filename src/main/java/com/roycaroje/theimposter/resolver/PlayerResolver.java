package com.roycaroje.theimposter.resolver;

import com.roycaroje.theimposter.service.PlayerService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PlayerResolver {

    private final PlayerService playerService;
}
