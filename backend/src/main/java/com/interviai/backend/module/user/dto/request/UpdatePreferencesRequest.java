package com.interviai.backend.module.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating email preference flags.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User email preference updates")
public class UpdatePreferencesRequest {

    @Schema(description = "Whether marketing emails are enabled")
    private Boolean marketingEmailsEnabled;

    @Schema(description = "Whether notification emails are enabled")
    private Boolean notificationEmailsEnabled;
}
