package com.roycaroje.theimposter.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "custom_word")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomWord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID roomPublicId;
    private UUID playerPublicId;
    private String word;
    private boolean picked;
}
