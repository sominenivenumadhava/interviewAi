package com.interviai.backend.module.user.entity;

import com.interviai.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * User preference entity for storing user-specific settings.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Entity
@Table(name = "user_preferences", indexes = {
    @Index(name = "idx_user_preferences_user_id", columnList = "user_id"),
    @Index(name = "idx_user_preferences_key", columnList = "preference_key")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserPreference extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "preference_key", nullable = false, length = 100)
    private String key;

    @Column(name = "preference_value", columnDefinition = "TEXT")
    private String value;

    @Column(name = "preference_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PreferenceType type = PreferenceType.STRING;

    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private Boolean isSystem = false;

    @Column(name = "description", length = 500)
    private String description;

    /**
     * Preference type enumeration.
     */
    public enum PreferenceType {
        STRING,
        INTEGER,
        BOOLEAN,
        JSON,
        DECIMAL
    }

    /**
     * Get value as string.
     */
    public String getStringValue() {
        return value;
    }

    /**
     * Get value as integer.
     */
    public Integer getIntegerValue() {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Get value as boolean.
     */
    public Boolean getBooleanValue() {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Boolean.valueOf(value);
    }

    /**
     * Get value as double.
     */
    public Double getDoubleValue() {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Set string value.
     */
    public void setStringValue(String value) {
        this.value = value;
        this.type = PreferenceType.STRING;
    }

    /**
     * Set integer value.
     */
    public void setIntegerValue(Integer value) {
        this.value = value != null ? value.toString() : null;
        this.type = PreferenceType.INTEGER;
    }

    /**
     * Set boolean value.
     */
    public void setBooleanValue(Boolean value) {
        this.value = value != null ? value.toString() : null;
        this.type = PreferenceType.BOOLEAN;
    }

    /**
     * Set double value.
     */
    public void setDoubleValue(Double value) {
        this.value = value != null ? value.toString() : null;
        this.type = PreferenceType.DECIMAL;
    }

    /**
     * Set JSON value.
     */
    public void setJsonValue(String jsonValue) {
        this.value = jsonValue;
        this.type = PreferenceType.JSON;
    }

    /**
     * Create string preference.
     */
    public static UserPreference createString(UUID userId, String key, String value) {
        return UserPreference.builder()
                .userId(userId)
                .key(key)
                .value(value)
                .type(PreferenceType.STRING)
                .build();
    }

    /**
     * Create boolean preference.
     */
    public static UserPreference createBoolean(UUID userId, String key, Boolean value) {
        return UserPreference.builder()
                .userId(userId)
                .key(key)
                .value(value != null ? value.toString() : null)
                .type(PreferenceType.BOOLEAN)
                .build();
    }

    /**
     * Create integer preference.
     */
    public static UserPreference createInteger(UUID userId, String key, Integer value) {
        return UserPreference.builder()
                .userId(userId)
                .key(key)
                .value(value != null ? value.toString() : null)
                .type(PreferenceType.INTEGER)
                .build();
    }

    /**
     * Create system preference.
     */
    public static UserPreference createSystem(UUID userId, String key, String value, String description) {
        return UserPreference.builder()
                .userId(userId)
                .key(key)
                .value(value)
                .type(PreferenceType.STRING)
                .isSystem(true)
                .description(description)
                .build();
    }
}