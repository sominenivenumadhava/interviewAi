package com.interviai.backend.module.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviai.backend.module.auth.dto.request.LoginRequest;
import com.interviai.backend.module.auth.dto.request.RefreshTokenRequest;
import com.interviai.backend.module.auth.dto.request.LogoutRequest;
import com.interviai.backend.module.auth.dto.response.AuthenticationResponse;
import com.interviai.backend.module.auth.dto.response.LogoutResponse;
import com.interviai.backend.module.auth.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthenticationController.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@WebMvcTest(AuthenticationController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AuthenticationService authenticationService;

        @MockBean
        private com.interviai.backend.module.auth.service.JwtService jwtService;

        @MockBean
        private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

        @Test
        void shouldLoginSuccessfully() throws Exception {
                // Given
                LoginRequest loginRequest = LoginRequest.builder()
                                .email("test@example.com")
                                .password("password123")
                                .build();

                AuthenticationResponse.UserInfo userInfo = AuthenticationResponse.UserInfo.builder()
                                .id(UUID.randomUUID())
                                .email("test@example.com")
                                .firstName("John")
                                .lastName("Doe")
                                .emailVerified(true)
                                .isActive(true)
                                .build();

                AuthenticationResponse authResponse = AuthenticationResponse.builder()
                                .accessToken("eyJhbGciOiJIUzI1NiIs...")
                                .refreshToken("550e8400-e29b-41d4-a716...")
                                .tokenType("Bearer")
                                .expiresIn(3600L)
                                .user(userInfo)
                                .build();

                when(authenticationService.authenticate(any(LoginRequest.class), anyString()))
                                .thenReturn(authResponse);

                // When & Then
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                                .with(csrf()))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Login successful"))
                                .andExpect(jsonPath("$.data.accessToken").value("eyJhbGciOiJIUzI1NiIs..."))
                                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                                .andExpect(jsonPath("$.data.user.email").value("test@example.com"));
        }

        @Test
        void shouldReturnBadRequestForInvalidEmail() throws Exception {
                // Given
                LoginRequest loginRequest = LoginRequest.builder()
                                .email("invalid-email")
                                .password("password123")
                                .build();

                // When & Then
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                                .with(csrf()))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }

        @Test
        void shouldReturnBadRequestForMissingPassword() throws Exception {
                // Given
                LoginRequest loginRequest = LoginRequest.builder()
                                .email("test@example.com")
                                .build();

                // When & Then
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                                .with(csrf()))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }

        @Test
        void shouldRefreshTokenSuccessfully() throws Exception {
                // Given
                RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                                .refreshToken("550e8400-e29b-41d4-a716...")
                                .build();

                AuthenticationResponse refreshResponse = AuthenticationResponse.builder()
                                .accessToken("new-access-token")
                                .tokenType("Bearer")
                                .expiresIn(3600L)
                                .issuedAt(LocalDateTime.now())
                                .build();

                when(authenticationService.refreshToken(any(RefreshTokenRequest.class), anyString()))
                                .thenReturn(refreshResponse);

                // When & Then
                mockMvc.perform(post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(refreshRequest))
                                .with(csrf()))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
        }

        @Test
        void shouldReturnBadRequestForMissingRefreshToken() throws Exception {
                // Given
                RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder().build();

                // When & Then
                mockMvc.perform(post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(refreshRequest))
                                .with(csrf()))
                                .andDo(print())
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }

        @Test
        @WithMockUser(username = "test@example.com")
        void shouldLogoutSuccessfully() throws Exception {
                // Given
                LogoutResponse logoutResponse = LogoutResponse.builder()
                                .message("Logout successful")
                                .build();
                when(authenticationService.logout(any(LogoutRequest.class), any(UUID.class)))
                                .thenReturn(logoutResponse);

                // When & Then
                mockMvc.perform(post("/api/v1/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                                .with(csrf()))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(username = "test@example.com")
        void shouldValidateTokenSuccessfully() throws Exception {
                // When & Then
                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/auth/validate")
                                .with(csrf()))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").value("Token is valid"));
        }
}