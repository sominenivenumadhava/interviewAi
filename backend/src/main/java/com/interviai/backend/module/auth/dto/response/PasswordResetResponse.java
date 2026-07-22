package com.interviai.backend.module.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for password reset operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Password reset response")
public class PasswordResetResponse {

    @Schema(description = "Operation success status", example = "true")
    @Builder.Default
    private Boolean success = true;

    @Schema(description = "Response message", example = "Password reset email sent successfully")
    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(description = "Request timestamp")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "Email address (masked for security)", example = "us**@example.com")
    private String maskedEmail;

    @Schema(description = "Token expiration time in minutes", example = "60")
    private Integer expirationMinutes;

    /**
     * Create successful forgot password response.
     */
    public static PasswordResetResponse forgotPasswordSuccess(String maskedEmail, int expirationMinutes) {
        return PasswordResetResponse.builder()
                .success(true)
                .message("Password reset email sent successfully")
                .maskedEmail(maskedEmail)
                .expirationMinutes(expirationMinutes)
                .build();
    }

    /**
     * Create successful password reset response.
     */
    public static PasswordResetResponse resetPasswordSuccess() {
        return PasswordResetResponse.builder()
                .success(true)
                .message("Password reset completed successfully")
                .build();
    }

    /**
     * Create response for email not found (for security, still show success).
     */
    public static PasswordResetResponse emailNotFoundSecure() {
        return PasswordResetResponse.builder()
                .success(true)
                .message("If the email exists in our system, you will receive a password reset link")
                .build();
    }
}