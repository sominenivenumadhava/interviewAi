package com.interviai.backend.module.user.service;

import com.interviai.backend.common.exception.ValidationException;
import com.interviai.backend.module.user.dto.request.RegisterRequest;
import com.interviai.backend.module.user.dto.response.UserResponse;
import com.interviai.backend.module.user.entity.User;
import com.interviai.backend.module.user.mapper.UserMapper;
import com.interviai.backend.module.user.repository.UserRepository;
import com.interviai.backend.module.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.interviai.backend.module.notification.service.EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest validRegisterRequest;
    private User mockUser;
    private UserResponse mockUserResponse;

    @BeforeEach
    void setUp() {
        validRegisterRequest = RegisterRequest.builder()
                .email("test@example.com")
                .username("testuser")
                .password("SecurePass123!")
                .confirmPassword("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .acceptTerms(true)
                .acceptPrivacy(true)
                .build();

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@example.com");
        mockUser.setUsername("testuser");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setEmailVerified(false);
        mockUser.setRole(User.UserRole.USER);

        mockUserResponse = UserResponse.builder()
                .id(mockUser.getId())
                .email(mockUser.getEmail())
                .username(mockUser.getUsername())
                .firstName(mockUser.getFirstName())
                .lastName(mockUser.getLastName())
                .emailVerified(mockUser.getEmailVerified())
                .role(mockUser.getRole())
                .build();
    }

    @Test
    @DisplayName("Should register user successfully with valid data")
    void shouldRegisterUserSuccessfully() {
        // Given
        when(userRepository.findByEmail(validRegisterRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userMapper.toEntity(validRegisterRequest)).thenReturn(mockUser);
        when(passwordEncoder.encode(validRegisterRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userMapper.toResponse(mockUser)).thenReturn(mockUserResponse);

        // When
        UserResponse result = userService.registerUser(validRegisterRequest);

        // Then
        assertNotNull(result);
        assertEquals(mockUser.getEmail(), result.getEmail());
        assertEquals(mockUser.getUsername(), result.getUsername());
        assertEquals(mockUser.getFirstName(), result.getFirstName());
        assertEquals(mockUser.getLastName(), result.getLastName());
        assertFalse(result.getEmailVerified());
        assertEquals(User.UserRole.USER, result.getRole());

        verify(userRepository).findByEmail(validRegisterRequest.getEmail());
        verify(userRepository).existsByUsername(anyString());
        verify(passwordEncoder).encode(validRegisterRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponse(mockUser);
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given — registration uses findByEmail (not existsByEmail) to support OAuth password set
        User existing = new User();
        existing.setId(UUID.randomUUID());
        existing.setEmail(validRegisterRequest.getEmail());
        existing.setPasswordHash("already-hashed");
        when(userRepository.findByEmail(validRegisterRequest.getEmail())).thenReturn(Optional.of(existing));

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.registerUser(validRegisterRequest)
        );

        assertTrue(exception.getMessage().contains("email"));
        verify(userRepository).findByEmail(validRegisterRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Given
        when(userRepository.findByEmail(validRegisterRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.registerUser(validRegisterRequest)
        );

        assertTrue(exception.getMessage().contains("username"));
        verify(userRepository).findByEmail(validRegisterRequest.getEmail());
        verify(userRepository).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should find user by email successfully")
    void shouldFindUserByEmailSuccessfully() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email.toLowerCase())).thenReturn(Optional.of(mockUser));

        // When
        Optional<User> result = userService.findByEmail(email);

        // Then
        assertTrue(result.isPresent());
        assertEquals(mockUser.getEmail(), result.get().getEmail());
        verify(userRepository).findByEmail(email.toLowerCase());
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void shouldReturnEmptyWhenUserNotFoundByEmail() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email.toLowerCase())).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail(email);

        // Then
        assertTrue(result.isEmpty());
        verify(userRepository).findByEmail(email.toLowerCase());
    }

    @Test
    @DisplayName("Should find user by username successfully")
    void shouldFindUserByUsernameSuccessfully() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username.toLowerCase())).thenReturn(Optional.of(mockUser));

        // When
        Optional<User> result = userService.findByUsername(username);

        // Then
        assertTrue(result.isPresent());
        assertEquals(mockUser.getUsername(), result.get().getUsername());
        verify(userRepository).findByUsername(username.toLowerCase());
    }

    @Test
    @DisplayName("Should check email availability correctly")
    void shouldCheckEmailAvailabilityCorrectly() {
        // Given
        String availableEmail = "available@example.com";
        String unavailableEmail = "taken@example.com";
        
        when(userRepository.existsByEmail(availableEmail.toLowerCase())).thenReturn(false);
        when(userRepository.existsByEmail(unavailableEmail.toLowerCase())).thenReturn(true);

        // When & Then
        assertTrue(userService.isEmailAvailable(availableEmail));
        assertFalse(userService.isEmailAvailable(unavailableEmail));

        verify(userRepository).existsByEmail(availableEmail.toLowerCase());
        verify(userRepository).existsByEmail(unavailableEmail.toLowerCase());
    }

    @Test
    @DisplayName("Should check username availability correctly")
    void shouldCheckUsernameAvailabilityCorrectly() {
        // Given
        String availableUsername = "availableuser";
        String unavailableUsername = "takenuser";
        
        when(userRepository.existsByUsername(availableUsername.toLowerCase())).thenReturn(false);
        when(userRepository.existsByUsername(unavailableUsername.toLowerCase())).thenReturn(true);

        // When & Then
        assertTrue(userService.isUsernameAvailable(availableUsername));
        assertFalse(userService.isUsernameAvailable(unavailableUsername));

        verify(userRepository).existsByUsername(availableUsername.toLowerCase());
        verify(userRepository).existsByUsername(unavailableUsername.toLowerCase());
    }

    @Test
    @DisplayName("Should return true for null or empty username availability")
    void shouldReturnTrueForNullOrEmptyUsernameAvailability() {
        // When & Then
        assertTrue(userService.isUsernameAvailable(null));
        assertTrue(userService.isUsernameAvailable(""));
        assertTrue(userService.isUsernameAvailable("   "));

        // Verify no repository calls were made
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should verify email successfully")
    void shouldVerifyEmailSuccessfully() {
        // Given
        String token = "valid-token";
        mockUser.generateEmailVerificationToken(token, 24);
        
        when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userMapper.toResponse(mockUser)).thenReturn(mockUserResponse);

        // When
        UserResponse result = userService.verifyEmail(token);

        // Then
        assertNotNull(result);
        verify(userRepository).findByEmailVerificationToken(token);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponse(mockUser);
    }

    @Test
    @DisplayName("Should throw exception for invalid verification token")
    void shouldThrowExceptionForInvalidVerificationToken() {
        // Given
        String invalidToken = "invalid-token";
        when(userRepository.findByEmailVerificationToken(invalidToken)).thenReturn(Optional.empty());

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.verifyEmail(invalidToken)
        );

        assertTrue(exception.getMessage().contains("Invalid or expired"));
        verify(userRepository).findByEmailVerificationToken(invalidToken);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should update last login successfully")
    void shouldUpdateLastLoginSuccessfully() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // When
        userService.updateLastLogin(userId);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle failed login attempt")
    void shouldHandleFailedLoginAttempt() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // When
        userService.handleFailedLogin(userId);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }
}