package com.example.auction.model;

import com.example.auction.util.IdGenerator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public abstract class Entity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private LocalDateTime createdAt;

    protected Entity() {
        this.id = IdGenerator.newId();
        this.createdAt = LocalDateTime.now();
    }

    protected Entity(String id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Entity entity)) {
            return false;
        }
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
