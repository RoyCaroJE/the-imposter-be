package com.roycaroje.theimposter.transformer;

import com.roycaroje.theimposter.model.CustomWord;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class CustomWordTransformer {

    public CustomWord createCustomWord(UUID playerPublicId, UUID roomPublicId, String word) {
        CustomWord customWord = new CustomWord();
        customWord.setPlayerPublicId(playerPublicId);
        customWord.setRoomPublicId(roomPublicId);
        customWord.setWord(word.trim());
        return customWord;
    }

    public List<CustomWord> createCustomWords(UUID playerPublicId, UUID roomPublicId, List<String> words) {
        List<CustomWord> customWords = new ArrayList<>();
        for (String word : words) {
            if (word != null && !word.isBlank()) {
                customWords.add(createCustomWord(playerPublicId, roomPublicId, word));
            }
        }
        return customWords;
    }
}

