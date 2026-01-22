package com.roycaroje.theimposter.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "player")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Player extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    private boolean admin = false;
    private boolean impostor = false;
    private boolean confirmed = false;
    private boolean connected = true;
    private UUID roomPublicId;
    private Instant lastSeen;
    private String word;
}
