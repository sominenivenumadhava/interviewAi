package com.interviai.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviai.backend.common.dto.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT authentication entry point to handle authentication errors.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        log.debug("Authentication failed for request: {} - {}", 
                request.getRequestURI(), authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponse errorResponse = ErrorResponse.authenticationError(
                determineErrorMessage(request, authException),
                request.getRequestURI()
        );

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    /**
     * Determine appropriate error message based on request and exception.
     */
    private String determineErrorMessage(HttpServletRequest request, AuthenticationException authException) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return "Authentication token required";
        }
        
        if (authException.getMessage() != null) {
            String message = authException.getMessage().toLowerCase();
            
            if (message.contains("expired")) {
                return "Authentication token has expired";
            } else if (message.contains("invalid")) {
                return "Invalid authentication token";
            } else if (message.contains("malformed")) {
                return "Malformed authentication token";
            }
        }
        
        return "Authentication required";
    }
}