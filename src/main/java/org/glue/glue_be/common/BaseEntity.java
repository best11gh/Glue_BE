package org.glue.glue_be.common;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.Getter;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@EntityListeners(value = AuditingEntityListener.class)
@MappedSuperclass
abstract public class BaseEntity {

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void touchUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

}
