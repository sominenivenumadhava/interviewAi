package com.interviai.backend.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.util.UUID;

/**
 * Auditable entity class providing audit fields for entities that require user tracking.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
public abstract class AuditableEntity extends BaseEntity {

    @CreatedBy
    @Column(name = "created_by")
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /**
     * Soft delete the entity.
     */
    public void softDelete() {
        this.isDeleted = true;
        this.isActive = false;
    }

    /**
     * Restore the soft deleted entity.
     */
    public void restore() {
        this.isDeleted = false;
        this.isActive = true;
    }

    /**
     * Deactivate the entity without deleting.
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Activate the entity.
     */
    public void activate() {
        this.isActive = true;
    }
}