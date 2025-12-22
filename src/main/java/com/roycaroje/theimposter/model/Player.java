package com.roycaroje.theimposter.model;

import jakarta.persistence.*;
import lombok.*;
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

    private Boolean admin = false;
    private Boolean impostor = false;
    private Boolean confirmed = false;
    private Boolean connected = true;
    private UUID roomPublicId;
}
