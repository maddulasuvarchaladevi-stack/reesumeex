package com.aiscreening.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Abstract base entity demonstrating OOP Abstraction and Inheritance.
 */
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final String id;
    protected final LocalDateTime createdAt;

    protected BaseEntity() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.createdAt = LocalDateTime.now();
    }

    protected BaseEntity(String id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getFormattedCreatedAt() {
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public abstract String getEntityType();
}
