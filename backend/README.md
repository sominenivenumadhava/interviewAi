# InterviAI Backend

## Overview

The backend for InterviAI is built with Spring Boot 3, Java 21, and follows enterprise-grade architecture patterns. It provides secure APIs for AI-powered interview preparation and assessment.

## Technology Stack

- **Language**: Java 21 (LTS)
- **Framework**: Spring Boot 3.2.1
- **Security**: Spring Security 6 + JWT
- **Database**: PostgreSQL 14+ with Liquibase migrations
- **ORM**: Spring Data JPA (Hibernate 6)
- **AI Integration**: Google Gemini API
- **Documentation**: OpenAPI 3 (Swagger)
- **Testing**: JUnit 5, Mockito, TestContainers
- **Build Tool**: Maven 3.9+
- **File Processing**: Apache PDFBox

## Project Structure

```
src/
├── main/
│   ├── java/com/interviai/backend/
│   │   ├── InterviAIApplication.java          # Main application class
│   │   ├── common/                            # ✅ Shared utilities and base classes
│   │   │   ├── entity/
│   │   │   │   ├── BaseEntity.java           # Base entity with common fields
│   │   │   │   └── AuditableEntity.java      # Auditable entity with tracking
│   │   │   ├── dto/
│   │   │   │   ├── ApiResponse.java          # Standard API response wrapper
│   │   │   │   ├── ErrorResponse.java        # Error response structure
│   │   │   │   ├── PageRequest.java          # Pagination request DTO
│   │   │   │   └── PageResponse.java         # Pagination response DTO
│   │   │   ├── exception/
│   │   │   │   ├── BusinessException.java    # Base business exception
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── ValidationException.java
│   │   │   │   ├── UnauthorizedException.java
│   │   │   │   ├── ForbiddenException.java
│   │   │   │   ├── ExternalServiceException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── util/
│   │   │   │   ├── DateUtil.java             # Date/time utilities
│   │   │   │   ├── StringUtil.java           # String manipulation utilities
│   │   │   │   └── ValidationUtil.java       # Validation helpers
│   │   │   └── constant/
│   │   │       ├── ApiConstants.java         # API-related constants
│   │   │       └── ErrorCodes.java           # Centralized error codes
│   │   ├── module/
│   │   │   └── auth/                         # ✅ Authentication module
│   │   │       ├── controller/
│   │   │       │   └── AuthenticationController.java
│   │   │       ├── service/
│   │   │       │   ├── AuthenticationService.java
│   │   │       │   ├── JwtService.java
│   │   │       │   └── RefreshTokenService.java
│   │   │       ├── repository/
│   │   │       │   └── RefreshTokenRepository.java
│   │   │       ├── entity/
│   │   │       │   └── RefreshToken.java
│   │   │       ├── dto/
│   │   │       │   ├── request/
│   │   │       │   └── response/
│   │   │       ├── mapper/
│   │   │       │   └── AuthMapper.java
│   │   │       └── exception/
│   │   ├── security/                         # Security configuration
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── CustomUserDetailsService.java
│   │   │   ├── SecurityConfig.java
│   │   │   ├── JwtAuthenticationEntryPoint.java
│   │   │   └── JwtAccessDeniedHandler.java
│   │   └── config/                           # Application configuration
│   │       ├── SwaggerConfig.java
│   │       └── JpaConfig.java
│   └── resources/
│       ├── application.yml                   # Multi-profile configuration
│       └── db/changelog/                     # Database migrations
│           ├── db.changelog-master.xml
│           └── 001-create-refresh-tokens-table.xml
└── test/                                     # Test classes
    └── java/com/interviai/backend/
        ├── common/
        │   └── util/
        │       └── StringUtilTest.java
        └── module/
            └── auth/
                ├── service/
                │   └── JwtServiceTest.java
                └── controller/
                    └── AuthenticationControllerTest.java
```

## Getting Started

### Prerequisites

- **Java 21** (OpenJDK or Oracle JDK)
- **Maven 3.9+**
- **PostgreSQL 14+**
- **Docker** (optional, for containerized development)

### Database Setup

1. **Install PostgreSQL**
   ```bash
   # Ubuntu/Debian
   sudo apt install postgresql postgresql-contrib
   
   # macOS with Homebrew
   brew install postgresql
   
   # Windows - Download from postgresql.org
   ```

2. **Create Database**
   ```bash
   # Create database and user
   sudo -u postgres createdb interviai_dev
   sudo -u postgres createuser --superuser interviai_user
   sudo -u postgres psql -c "ALTER USER interviai_user PASSWORD 'interviai_password';"
   ```

3. **Configure Database Connection**
   Create `src/main/resources/application-local.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/interviai_dev
       username: interviai_user
       password: interviai_password
   ```

### Environment Configuration

Create environment variables or `application-local.yml`:

```yaml
spring:
  profiles:
    active: local

app:
  jwt:
    secret: ${JWT_SECRET:}  # required in prod; set local default only under spring.profiles=dev
    issuer: interviai-backend
  
openrouter:
  api:
    key: ${OPENROUTER_API_KEY:sk-or-v1-your-openrouter-key}
    model: ${OPENROUTER_MODEL:google/gemini-2.5-flash-lite}

logging:
  level:
    com.interviai.backend: DEBUG
```

### Running the Application

```bash
# Clone and navigate to backend directory
cd backend

# Install dependencies and run
mvn clean install
mvn spring-boot:run

# Or run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The backend will be available at `http://localhost:8082`

## API Documentation

Once the application is running, access:

- **Swagger UI**: `http://localhost:8082/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8082/v3/api-docs`
- **Health Check**: `http://localhost:8082/actuator/health`

## Available Maven Commands

```bash
# Development
mvn spring-boot:run                    # Start application
mvn spring-boot:run -Dspring.profiles.active=dev  # Start with dev profile

# Building
mvn clean compile                      # Compile source code
mvn clean package                      # Build JAR file
mvn clean install                      # Install to local repository

# Testing
mvn test                              # Run unit tests
mvn verify                            # Run all tests including integration
mvn test -Dtest=AuthenticationServiceTest  # Run specific test

# Database
mvn liquibase:update                  # Apply database migrations
mvn liquibase:rollback -Dliquibase.rollbackCount=1  # Rollback last migration
mvn liquibase:status                  # Check migration status

# Code Quality
mvn checkstyle:check                  # Check code style
mvn spotbugs:check                    # Static analysis
mvn jacoco:report                     # Generate test coverage report

# Docker
mvn spring-boot:build-image           # Build Docker image
```

## Architecture Overview

### Layered Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    REST Controllers                          │
├─────────────────────────────────────────────────────────────┤
│                    Service Layer                            │
├─────────────────────────────────────────────────────────────┤
│                    Repository Layer                         │
├─────────────────────────────────────────────────────────────┤
│                    Database (PostgreSQL)                   │
└─────────────────────────────────────────────────────────────┘
```

### Module Architecture

Each module follows this structure:
- **Controller**: REST endpoints, request validation
- **Service**: Business logic, transaction management
- **Repository**: Data access, query operations
- **Entity**: Database mapping, business methods
- **DTO**: Data transfer objects for API
- **Mapper**: Entity-DTO conversion (MapStruct)

## Current Implementation Status

### ✅ Common Module (Complete)
- Base entities with audit trails
- Standardized API responses (`ApiResponse<T>`)
- Global exception handling with structured errors
- Comprehensive utility classes (`StringUtil`, `DateUtil`, `ValidationUtil`)
- Centralized error codes and constants
- Pagination support

### ✅ Authentication Module (Complete)
- JWT-based authentication with HS256 signing
- Refresh token management with database storage
- Multi-device session support
- Password reset functionality (email integration ready)
- Role-based authorization (USER, ADMIN)
- Security configuration with CORS support

**API Endpoints:**
- `POST /api/v1/auth/login` - User authentication
- `POST /api/v1/auth/refresh` - Token refresh
- `POST /api/v1/auth/logout` - User logout
- `POST /api/v1/auth/forgot-password` - Password reset initiation
- `POST /api/v1/auth/reset-password` - Password reset completion
- `GET /api/v1/auth/validate` - Token validation
- `GET /api/v1/auth/me` - Current user info

### 🚧 Planned Modules

1. **User Module** (Next)
   - User registration and profiles
   - Email verification
   - User preferences and settings

2. **Resume Module**
   - PDF upload and parsing (Apache PDFBox)
   - Content extraction and analysis
   - Resume storage and management

3. **Interview Module**
   - Interview session management
   - Interview configuration and workflow
   - State machine implementation

4. **AI Integration Modules**
   - Question generation (Gemini API)
   - Answer evaluation and scoring
   - Feedback generation

## Database Design

### Current Schema

#### refresh_tokens
```sql
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT false,
    user_agent VARCHAR(1000),
    ip_address VARCHAR(45),
    device_id VARCHAR(255),
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Indexes for performance
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
```

### Migration Management

Using Liquibase for database versioning:

```xml
<!-- db.changelog-master.xml -->
<databaseChangeLog>
    <include file="db/changelog/001-create-refresh-tokens-table.xml"/>
    <!-- Future migrations will be added here -->
</databaseChangeLog>
```

## Security Implementation

### JWT Configuration

- **Algorithm**: HS256 (HMAC SHA-256)
- **Access Token**: 1 hour validity
- **Refresh Token**: 30 days validity, stored in database
- **Claims**: User ID, email, roles, token type

### Security Features

1. **Authentication**: JWT-based stateless authentication
2. **Authorization**: Role-based access control (RBAC)
3. **Password Security**: BCrypt hashing with cost factor 12
4. **Session Management**: Multi-device support with token tracking
5. **CORS**: Configured for multiple environments
6. **Rate Limiting**: Infrastructure ready for implementation

### Endpoint Security

```java
// Public endpoints (no authentication required)
/api/v1/auth/login
/api/v1/auth/refresh
/api/v1/auth/forgot-password
/api/v1/auth/reset-password
/api/v1/users/register

// Protected endpoints (authentication required)
/api/v1/users/**
/api/v1/interviews/**
/api/v1/resumes/**

// Admin endpoints (ADMIN role required)
/api/v1/admin/**
/actuator/** (except health/info)
```

## Testing Strategy

### Unit Tests

```java
// Example test structure
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    
    @Mock
    private SomeService someService;
    
    @InjectMocks
    private JwtService jwtService;
    
    @Test
    @DisplayName("Should generate valid access token")
    void shouldGenerateValidAccessToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        
        // When
        String token = jwtService.generateAccessToken(userId, email, "USER");
        
        // Then
        assertThat(token).isNotNull();
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
    }
}
```

### Integration Tests

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthenticationControllerIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldLoginSuccessfully() {
        // Integration test implementation
    }
}
```

### Test Coverage Goals

- **Unit Tests**: 85%+ coverage
- **Integration Tests**: 70%+ coverage
- **Security Tests**: 90%+ coverage

## Performance Considerations

### Database Optimization

```yaml
# HikariCP connection pool configuration
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1800000
      connection-timeout: 20000
      leak-detection-threshold: 60000
```

### JVM Optimization

```bash
# Production JVM settings
JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### Caching Strategy (Future)

```java
@Cacheable(value = "users", key = "#email")
public User findByEmail(String email) {
    // Method implementation
}
```

## Monitoring and Observability

### Actuator Endpoints

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized
```

### Available Endpoints

- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics (when enabled)

### Logging Configuration

```yaml
logging:
  level:
    com.interviai.backend: INFO
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/interviai-backend.log
    max-size: 10MB
    max-history: 30
```

## Deployment

### Docker Configuration

```dockerfile
# Dockerfile (to be created)
FROM openjdk:21-jre-slim

WORKDIR /app
COPY target/backend-1.0.0.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Environment Variables

```bash
# Required environment variables
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password  
JWT_SECRET=your-256-bit-secret-key
OPENROUTER_API_KEY=sk-or-v1-your-openrouter-key
OPENROUTER_MODEL=google/gemini-2.5-flash-lite

# Optional environment variables
SPRING_PROFILES_ACTIVE=prod
LOG_LEVEL=INFO
```

### Health Checks

```yaml
# docker-compose.yml health check
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8082/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
```

## Development Guidelines

### Code Style

- Follow Google Java Style Guide
- Use Lombok for boilerplate code reduction
- Implement proper JavaDoc for public APIs
- Use meaningful variable and method names

### Git Workflow

```bash
# Feature branch workflow
git checkout -b feature/user-registration
git commit -m "feat(user): implement user registration endpoint"
git push origin feature/user-registration
```

### Commit Message Format

```
type(scope): description

[optional body]

[optional footer]
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

## API Integration Guide

### Making Authenticated Requests

```bash
# Login to get token
curl -X POST http://localhost:8082/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# Use token for authenticated requests
curl -X GET http://localhost:8082/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Error Response Format

```json
{
  "code": "AUTH001",
  "message": "Invalid email or password",
  "timestamp": "2024-01-15T10:45:00.000Z",
  "path": "/api/v1/auth/login",
  "requestId": "550e8400-e29b-41d4-a716-446655440000"
}
```

## Troubleshooting

### Common Issues

1. **Database Connection Errors**
   ```bash
   # Check PostgreSQL service
   sudo systemctl status postgresql
   
   # Check connection
   psql -h localhost -U interviai_user -d interviai_dev
   ```

2. **JWT Token Issues**
   ```bash
   # Check JWT secret configuration
   # Ensure it's at least 256 bits (32 characters)
   ```

3. **Port Already in Use**
   ```bash
   # Change port in application.yml
   server:
     port: 8081
   ```

### Debug Configuration

```yaml
logging:
  level:
    com.interviai.backend: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

## Contributing

1. Follow the established module structure
2. Write comprehensive tests for new features
3. Update documentation for API changes
4. Use proper error handling and logging
5. Follow security best practices

---

This backend provides a solid foundation for the InterviAI platform with enterprise-grade security, scalable architecture, and comprehensive documentation. The modular design allows for easy extension and maintenance as new features are added.
