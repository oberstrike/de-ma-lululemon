package com.mediaserver.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(name = "mega_path")
    private String megaPath;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<MovieJpaEntity> movies = new ArrayList<>();
}
