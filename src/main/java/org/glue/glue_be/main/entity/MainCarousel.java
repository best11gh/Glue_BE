package org.glue.glue_be.main.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.glue.glue_be.common.BaseEntity;

@Entity
@Table(name = "main_carousel")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MainCarousel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "description")
    private String description;

    @Builder
    private MainCarousel(String imageUrl, String version, Integer displayOrder, String description) {
        this.imageUrl = imageUrl;
        this.version = version;
        this.displayOrder = displayOrder;
        this.description = description;
    }
}