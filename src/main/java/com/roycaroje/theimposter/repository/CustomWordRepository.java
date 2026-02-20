package com.roycaroje.theimposter.repository;

import com.roycaroje.theimposter.model.CustomWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomWordRepository extends JpaRepository<CustomWord, Long> {
    List<CustomWord> findAllByRoomPublicIdAndPickedIsFalse(UUID roomPublicId);
}

