package org.glue.glue_be.main.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.glue.glue_be.common.BaseEntity;

@Entity
@Table(name = "carousel_deploy_version")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CarouselDeployVersion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "version", nullable = false, unique = true)
    private String version;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "description")
    private String description;

    @Builder
    private CarouselDeployVersion(String version, Boolean isActive, String description) {
        this.version = version;
        this.isActive = isActive != null ? isActive : true;
        this.description = description;
    }

    public void updateVersion(String version, String description) {
        if (version != null) this.version = version;
        if (description != null) this.description = description;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}