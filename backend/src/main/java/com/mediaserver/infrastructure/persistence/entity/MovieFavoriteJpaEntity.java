package com.mediaserver.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "movie_favorites",
        uniqueConstraints = @UniqueConstraint(columnNames = {"movie_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieFavoriteJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    @ToString.Exclude
    private MovieJpaEntity movie;

    @Column(name = "user_id", nullable = false)
    private String userId;
}
