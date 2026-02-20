package com.roycaroje.theimposter.resolver;

import com.roycaroje.theimposter.service.CustomWordService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@AllArgsConstructor
public class CustomWordResolver {

    private final CustomWordService customWordService;

    @MutationMapping
    public boolean addWords(@Argument UUID playerPublicId, @Argument UUID roomPublicId, @Argument List<String> words) {
        return customWordService.addWords(playerPublicId, roomPublicId, words);
    }

    @MutationMapping
    public boolean startWordSubmission(@Argument UUID roomPublicId) {
        return customWordService.startWordSubmission(roomPublicId);
    }
}
