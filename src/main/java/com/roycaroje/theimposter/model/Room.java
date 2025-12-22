package com.roycaroje.theimposter.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 6, nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String status = "CREATED";
    private UUID adminPublicId;
    private Integer numImpostors;
    private String word;
    private Instant startedAt;
    private String roomName;
}
