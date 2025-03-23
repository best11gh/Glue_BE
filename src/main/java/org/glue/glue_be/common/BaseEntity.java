package org.glue.glue_be.common;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(value = AuditingEntityListener.class)
@MappedSuperclass
abstract public class BaseEntity {

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean isDeleted = false;

    public void updateDeleted(){
        this.isDeleted = true;
    }

    public void updateUnDeleted() {
        this.isDeleted = false;
    }

    public abstract void delete();
}
