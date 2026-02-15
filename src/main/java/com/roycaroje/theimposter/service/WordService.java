package com.roycaroje.theimposter.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roycaroje.theimposter.model.Difficulty;
import com.roycaroje.theimposter.model.Language;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.*;

@Service
public class WordService {

    private Map<Language, Map<Difficulty, List<String>>> words;
    private final Random random = new Random();

    @PostConstruct
    public void loadWords() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            InputStream is = new ClassPathResource("words.json").getInputStream();

            words = mapper.readValue(
                    is,
                    new TypeReference<Map<Language, Map<Difficulty, List<String>>>>() {}
            );

            System.out.println("Words loaded successfully at startup.");

        } catch (Exception e) {
            throw new RuntimeException("Failed to load words.json", e);
        }
    }

    public List<String> getWords(Language language, Difficulty difficulty) {

        if (difficulty == Difficulty.MIXED) {
            return getMixedWords(language);
        }

        return words.get(language).get(difficulty);
    }

    public String getRandomWord(Language language, Difficulty difficulty) {
        List<String> list = getWords(language, difficulty);

        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("No words available.");
        }

        return list.get(random.nextInt(list.size()));
    }

    private List<String> getMixedWords(Language language) {

        List<String> mixed = new ArrayList<>();
        Map<Difficulty, List<String>> difficulties = words.get(language);

        for (Difficulty diff : Difficulty.values()) {
            if (diff != Difficulty.MIXED) {
                mixed.addAll(difficulties.get(diff));
            }
        }

        return mixed;
    }
}
