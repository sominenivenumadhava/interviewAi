# Common Module - InterviAI Backend

## Overview

The Common Module provides the foundational components and utilities that are shared across all other modules in the InterviAI backend application. This module implements cross-cutting concerns and establishes the architectural patterns used throughout the system.

## Module Structure

```
com.interviai.backend.common/
├── entity/
│   ├── BaseEntity.java           # Base entity with common fields
│   └── AuditableEntity.java      # Auditable entity with tracking fields
├── dto/
│   ├── ApiResponse.java          # Standard API response wrapper
│   ├── ErrorResponse.java        # Error response structure
│   ├── PageRequest.java          # Pagination request DTO
│   └── PageResponse.java         # Pagination response DTO
├── exception/
│   ├── BusinessException.java         # Base business exception
│   ├── ResourceNotFoundException.java # Resource not found exception
│   ├── ValidationException.java       # Validation exception
│   ├── UnauthorizedException.java     # Authentication exception
│   ├── ForbiddenException.java        # Authorization exception
│   ├── ExternalServiceException.java  # External service exception
│   └── GlobalExceptionHandler.java    # Global exception handler
├── util/
│   ├── DateUtil.java             # Date/time utility functions
│   ├── StringUtil.java           # String manipulation utilities
│   └── ValidationUtil.java       # Validation helper methods
└── constant/
    ├── ApiConstants.java         # API-related constants
    └── ErrorCodes.java           # Centralized error codes
```

## Key Components

### 1. Base Entities

#### BaseEntity
- Provides common fields: `id`, `createdAt`, `updatedAt`, `version`
- Uses UUID as primary key
- Implements optimistic locking with `@Version`
- Automatic timestamp management with JPA auditing

#### AuditableEntity
- Extends `BaseEntity`
- Adds audit fields: `createdBy`, `updatedBy`, `isActive`, `isDeleted`
- Supports soft delete functionality
- Provides convenience methods for activation/deactivation

### 2. DTOs and Response Wrappers

#### ApiResponse<T>
- Standard response wrapper for all API endpoints
- Consistent structure: `success`, `message`, `data`, `metadata`, `timestamp`
- Factory methods for success and error responses

#### ErrorResponse
- Structured error response with field-level details
- Includes error codes, messages, and validation details
- Support for request tracing with `requestId`

#### Pagination Support
- `PageRequest`: Input DTO for pagination with sorting
- `PageResponse<T>`: Output DTO with pagination metadata
- Seamless integration with Spring Data

### 3. Exception Hierarchy

#### BusinessException (Base)
- Foundation for all business logic exceptions
- Supports error codes and parameterized messages
- Integrates with global exception handler

#### Specialized Exceptions
- `ResourceNotFoundException`: For missing resources
- `ValidationException`: For input validation failures
- `UnauthorizedException`: For authentication issues
- `ForbiddenException`: For authorization issues
- `ExternalServiceException`: For external service failures

#### GlobalExceptionHandler
- Centralized exception handling for all controllers
- Converts exceptions to standardized error responses
- Includes request tracing and logging
- Handles validation, authentication, and system errors

### 4. Utility Classes

#### StringUtil
- String manipulation and validation utilities
- Email and phone number validation
- Text transformation (camelCase, snake_case, etc.)
- Masking sensitive information
- Slug generation and normalization

#### DateUtil
- Date and time manipulation utilities
- Timezone handling and UTC conversion
- Date range validation
- Formatting and parsing with standard patterns

#### ValidationUtil
- Input validation helper methods
- Custom validation predicates
- File validation (size, extension, name)
- Business rule validation

### 5. Constants

#### ApiConstants
- API endpoint constants
- HTTP headers and content types
- Pagination defaults
- File upload constraints
- Security settings

#### ErrorCodes
- Centralized error code definitions
- Categorized by functional area
- Consistent error code format

## Usage Examples

### 1. Creating a Base Entity

```java
@Entity
@Table(name = "users")
public class User extends AuditableEntity {
    
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    // Additional fields and methods
}
```

### 2. Using Standard API Response

```java
@RestController
public class UserController {
    
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable UUID id) {
        UserResponse user = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
```

### 3. Custom Exception Usage

```java
@Service
public class UserService {
    
    public User findById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> ResourceNotFoundException.user(id));
    }
}
```

### 4. Validation Helper Usage

```java
@Service
public class UserService {
    
    public User createUser(CreateUserRequest request) {
        ValidationUtil.validateEmail(request.getEmail(), "email");
        ValidationUtil.validatePassword(request.getPassword(), "password");
        
        // Business logic
    }
}
```

### 5. String Utility Usage

```java
// Email masking
String maskedEmail = StringUtil.maskEmail("user@example.com"); // "us*@example.com"

// Slug generation
String slug = StringUtil.toSlug("Hello World!"); // "hello-world"

// Format validation
boolean isValid = StringUtil.isValidEmail("test@example.com"); // true
```

## Testing

### Unit Tests Included

- `StringUtilTest.java`: Comprehensive tests for string utilities
- Tests cover edge cases and validation scenarios
- 100% code coverage for utility methods

### Test Structure

```java
@Test
void testIsValidEmail() {
    assertTrue(StringUtil.isValidEmail("test@example.com"));
    assertFalse(StringUtil.isValidEmail("invalid-email"));
}
```

## Integration Points

### 1. JPA Auditing

```java
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    
    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return new SpringSecurityAuditorAware();
    }
}
```

### 2. Global Exception Handling

All exceptions thrown in the application are automatically handled by `GlobalExceptionHandler`, ensuring consistent error responses across all endpoints.

### 3. Validation Integration

The utility classes integrate seamlessly with Bean Validation and Spring's validation framework.

## Configuration

### Maven Dependencies

All required dependencies are included in the main `pom.xml`:
- Spring Boot Starters
- Lombok for code generation
- Jackson for JSON processing
- Bean Validation (JSR-303)

### Application Properties

Common configuration is handled through `application.yml`:
- Database settings
- Security configuration  
- File upload limits
- Pagination defaults

## Best Practices

### 1. Entity Design
- Always extend `BaseEntity` or `AuditableEntity`
- Use meaningful table and column names
- Implement proper equals/hashCode based on business keys

### 2. Exception Handling
- Use specific exception types for different error scenarios
- Include meaningful error messages and codes
- Never expose internal system details in error messages

### 3. Validation
- Validate at service layer boundaries
- Use utility methods for common validations
- Combine Bean Validation with custom validation logic

### 4. API Design
- Always wrap responses in `ApiResponse<T>`
- Use consistent error response format
- Include pagination metadata for list endpoints

### 5. Utility Usage
- Prefer utility methods over inline implementations
- Handle null values gracefully
- Use appropriate utility for specific operations

## Security Considerations

### 1. Data Masking
- Use `StringUtil.maskEmail()` and `maskPhoneNumber()` for logging
- Never log sensitive information in plain text

### 2. Input Validation
- Always validate external inputs using `ValidationUtil`
- Sanitize strings before processing
- Validate file uploads thoroughly

### 3. Error Messages
- Avoid exposing internal system information
- Use generic error messages for security-sensitive operations
- Log detailed errors for debugging (but not in responses)

## Performance Considerations

### 1. Entity Loading
- Use appropriate fetch strategies
- Implement pagination for large result sets
- Use projections when full entities are not needed

### 2. Caching
- Cache frequently accessed, slow-changing data
- Use appropriate cache eviction strategies
- Monitor cache hit ratios

### 3. Database Optimization
- Use proper indexing strategies
- Implement connection pooling
- Monitor query performance

## Migration Guide

When upgrading the Common Module:

1. **Database Changes**: Run Liquibase migrations
2. **API Changes**: Update client integrations if response format changes
3. **Validation Changes**: Update validation rules gradually
4. **Utility Changes**: Test existing integrations thoroughly

## Troubleshooting

### Common Issues

1. **UUID Generation**: Ensure database supports UUID generation
2. **Timezone Issues**: Always use UTC for stored timestamps
3. **Validation Failures**: Check validation rules and error messages
4. **Performance**: Monitor entity loading and query patterns

### Debug Configuration

```yaml
logging:
  level:
    com.interviai.backend.common: DEBUG
```

This will enable detailed logging for all common module operations.

## Future Enhancements

### Planned Features

1. **Caching Integration**: Redis support for distributed caching
2. **Event Publishing**: Application event framework
3. **Metrics Collection**: Custom metrics for business operations
4. **Advanced Validation**: Complex business rule validation framework
5. **Internationalization**: Multi-language error messages

### Extension Points

The module is designed for extension:
- New exception types can extend `BusinessException`
- Additional utility classes can follow established patterns
- New constants can be added to existing constant classes
- Custom validation rules can extend `ValidationUtil`

---

## Conclusion

The Common Module provides a solid foundation for the InterviAI backend, establishing consistent patterns and practices across all modules. It promotes code reuse, maintainability, and consistency while providing essential cross-cutting functionality.

All subsequent modules will build upon these foundations, ensuring a cohesive and maintainable codebase that follows enterprise-grade patterns and practices.