package com.roycaroje.theimposter.model;
import com.roycaroje.theimposter.graphql.room.RoomStatus;
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
    private String status = RoomStatus.CREATED.toString();
    private UUID adminPublicId;
    private Integer numImpostors;
    private String word;
    private Instant startedAt;
}
