package com.roycaroje.theimposter.service;

import com.roycaroje.theimposter.repository.CustomWordRepository;
import com.roycaroje.theimposter.repository.PlayerRepository;
import com.roycaroje.theimposter.repository.RoomRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class CustomWordCleanupService {

    private final CustomWordRepository customWordRepository;
    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;

    @Scheduled(fixedRate = 300000) // 5 minutes = 300000 milliseconds
    @Transactional
    public void cleanupOrphanedCustomWords() {
        log.info("Starting cleanup of orphaned custom words");

        customWordRepository.findAll().forEach(customWord -> {
            boolean roomExists = roomRepository.findByPublicId(customWord.getRoomPublicId()).isPresent();
            boolean playerExists = playerRepository.findByPublicId(customWord.getPlayerPublicId()).isPresent();

            if (!roomExists || !playerExists) {
                customWordRepository.delete(customWord);
                log.debug("Deleted orphaned custom word with id: {}", customWord.getId());
            }
        });

        log.info("Cleanup of orphaned custom words completed");
    }
}

