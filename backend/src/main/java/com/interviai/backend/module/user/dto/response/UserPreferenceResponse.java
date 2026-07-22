package com.interviai.backend.module.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.interviai.backend.module.user.entity.UserPreference;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for user preferences.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User preference response")
public class UserPreferenceResponse {

    @Schema(description = "Preference ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Preference key", example = "theme")
    private String key;

    @Schema(description = "Preference value", example = "dark")
    private String value;

    @Schema(description = "Preference type", example = "STRING")
    private UserPreference.PreferenceType type;

    @Schema(description = "Is system preference", example = "false")
    private Boolean isSystem;

    @Schema(description = "Preference description", example = "User interface theme preference")
    private String description;

    /**
     * Preferences collection response.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "User preferences collection response")
    public static class PreferencesResponse {

        @Schema(description = "User preferences as key-value pairs")
        private Map<String, Object> preferences;

        @Schema(description = "System preferences as key-value pairs")
        private Map<String, Object> systemPreferences;

        @Schema(description = "Total number of preferences")
        private Integer totalCount;

        @Schema(description = "Number of user preferences")
        private Integer userPreferencesCount;

        @Schema(description = "Number of system preferences")
        private Integer systemPreferencesCount;
    }
}