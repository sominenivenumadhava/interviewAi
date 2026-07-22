# InterviAI - AI-Powered Interview Platform

## Overview

InterviAI is an AI-powered personalized virtual interview preparation and assessment platform that helps candidates improve their interview skills through realistic AI-driven practice sessions.

## Project Structure

```
├── frontend/          # React + TypeScript frontend application
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── vite.config.ts
├── backend/           # Spring Boot backend application  
│   ├── src/
│   ├── pom.xml
│   └── docs/
├── docs/              # Project documentation
└── README.md          # This file
```

## Technology Stack

### Frontend
- **Framework**: React 18 with TypeScript
- **Build Tool**: Vite
- **Styling**: Tailwind CSS
- **State Management**: React Context + Hooks
- **HTTP Client**: Fetch API
- **Development**: Hot reload, ESLint, TypeScript checking

### Backend
- **Language**: Java 21
- **Framework**: Spring Boot 3.2
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL with Liquibase migrations
- **ORM**: Spring Data JPA (Hibernate)
- **AI Integration**: Google Gemini API
- **Documentation**: OpenAPI/Swagger
- **Testing**: JUnit 5, Mockito, TestContainers

## Getting Started

### Prerequisites

- **Node.js** 18+ (for frontend)
- **Java** 21+ (for backend)
- **PostgreSQL** 14+ (for database)
- **Maven** 3.9+ (for backend build)

### Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

The frontend will be available at `http://localhost:5173`

### Backend Setup

1. **Database Setup**
```bash
# Create PostgreSQL database
createdb interviai_dev
```

2. **Environment Variables**
```bash
# Create application-local.yml or set environment variables
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_db_password
export JWT_SECRET=your-256-bit-secret-key
export GEMINI_API_KEY=your-gemini-api-key
```

3. **Run Backend**
```bash
cd backend
mvn spring-boot:run
```

The backend API will be available at `http://localhost:8080`

### API Documentation

Once the backend is running, access the Swagger UI at:
`http://localhost:8080/swagger-ui/index.html`

## Features

### Current Implementation

#### ✅ Common Module
- Base entities with audit trails
- Standardized API responses
- Global exception handling
- Comprehensive utility classes
- Error codes and constants

#### ✅ Authentication Module  
- JWT-based authentication
- Refresh token management
- Password reset functionality
- Multi-device session support
- Role-based authorization

### Planned Modules

#### 🚧 User Module (Next)
- User registration and profiles
- Email verification
- User preferences and settings
- Profile management

#### 📋 Upcoming Modules
- Resume Module (PDF parsing, content extraction)
- Interview Module (session management, workflow)
- Question Module (AI-powered question generation)
- Answer Module (answer submission, validation)
- Evaluation Module (AI assessment, scoring)
- Analytics Module (performance metrics, insights)
- Dashboard Module (user dashboard, reports)

## Development

### Code Quality

- **Backend**: 85%+ test coverage target
- **Frontend**: ESLint + TypeScript strict mode
- **API**: OpenAPI documentation for all endpoints
- **Database**: Liquibase migrations for schema changes

### Architecture Principles

- **Clean Architecture**: Clear separation of concerns
- **SOLID Principles**: Maintainable and extensible code
- **Domain-Driven Design**: Business logic encapsulation
- **REST Standards**: Consistent API design
- **Security First**: Enterprise-grade security practices

### Development Workflow

1. **Feature Development**: Create feature branch from `develop`
2. **Testing**: Write tests for new functionality
3. **Code Review**: Peer review before merging
4. **Integration**: Merge to `develop` branch
5. **Deployment**: Deploy from `main` branch

## Environment Configuration

### Development
- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`  
- Database: Local PostgreSQL

### Production
- Frontend: `https://app.interviai.com`
- Backend: `https://api.interviai.com`
- Database: Managed PostgreSQL service

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support and questions:
- 📧 Email: support@interviai.com
- 📖 Documentation: [Project Wiki](docs/)
- 🐛 Issues: [GitHub Issues](https://github.com/interviai/platform/issues)

---

## Quick Commands

### Frontend
```bash
npm run dev      # Start development server
npm run build    # Build for production
npm run preview  # Preview production build
npm run lint     # Run ESLint
npm run type-check # TypeScript checking
```

### Backend
```bash
mvn spring-boot:run        # Start development server
mvn clean package          # Build JAR file
mvn test                   # Run tests
mvn spring-boot:build-info # Generate build info
```

### Database
```bash
mvn liquibase:update       # Apply database migrations
mvn liquibase:rollback     # Rollback last migration
mvn liquibase:status       # Check migration status
```
