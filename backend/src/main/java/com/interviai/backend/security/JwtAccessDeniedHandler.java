package com.interviai.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviai.backend.common.dto.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT access denied handler to handle authorization errors.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, 
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        log.warn("Access denied for user: {} on resource: {} - {}", 
                authentication != null ? authentication.getName() : "anonymous",
                request.getRequestURI(), 
                accessDeniedException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ErrorResponse errorResponse = ErrorResponse.authorizationError(
                determineErrorMessage(request, authentication, accessDeniedException),
                request.getRequestURI()
        );

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    /**
     * Determine appropriate error message based on request and context.
     */
    private String determineErrorMessage(HttpServletRequest request, 
                                       Authentication authentication,
                                       AccessDeniedException accessDeniedException) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "Authentication required";
        }
        
        String requestUri = request.getRequestURI();
        
        // Provide specific messages for different endpoints
        if (requestUri.startsWith("/api/v1/admin/")) {
            return "Administrative privileges required";
        } else if (requestUri.contains("/users/") && !requestUri.contains("/profile")) {
            return "Access denied to user resource";
        } else if (requestUri.startsWith("/actuator/")) {
            return "Administrative privileges required for system monitoring";
        }
        
        // Generic access denied message
        return "Access denied. You don't have permission to access this resource";
    }
}