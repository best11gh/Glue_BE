package org.glue.glue_be.common;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.Getter;
import org.glue.glue_be.common.config.LocalDateTimeStringConverter;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@EntityListeners(value = AuditingEntityListener.class)
@MappedSuperclass
abstract public class BaseEntity {

    @CreatedDate
    @Convert(converter = LocalDateTimeStringConverter.class)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Convert(converter = LocalDateTimeStringConverter.class)
    private LocalDateTime updatedAt;

}
