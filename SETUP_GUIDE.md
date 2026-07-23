# InterviAI - Complete Setup Guide

## Prerequisites

- **Java 21+** (for backend)
- **Node.js 18+** (for frontend)
- **PostgreSQL 14+** (for database)
- **Maven 3.9+** (for backend build)

## Backend Setup

### 1. Database Setup

```bash
# Create PostgreSQL database
createdb interviai_dev
```

### 2. Environment Variables

Create `backend/src/main/resources/application-local.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/interviai_dev
    username: your_db_username
    password: your_db_password

jwt:
  secret: your-256-bit-secret-key-here-must-be-at-least-256-bits

openrouter:
  api:
    key: sk-or-v1-your-openrouter-key
    model: google/gemini-2.5-flash-lite

spring:
  mail:
    username: your-email@gmail.com
    password: your-app-password
```

### 3. Run Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run -Dspring.profiles.active=local
```

The backend will be available at `http://localhost:8080`

### API Documentation

Once running, access Swagger UI at: `http://localhost:8080/swagger-ui/index.html`

## Frontend Setup

### 1. Install Dependencies

```bash
cd frontend
npm install
```

### 2. Run Frontend

```bash
npm run dev
```

The frontend will be available at `http://localhost:5173`

## Complete Application Architecture

### Backend Modules (All Implemented ✅)

1. **Common Module** - Base entities, utilities, exception handling
2. **Authentication Module** - JWT authentication, refresh tokens
3. **User Module** - User management, profile, email notifications
4. **Resume Module** - Resume parsing with AI integration
5. **Interview Module** - Interview sessions, questions, answers
6. **Evaluation Module** - AI-powered assessment and scoring
7. **Analytics Module** - Performance metrics and insights
8. **Dashboard Module** - User dashboard and summaries
9. **AI Module** - Google Gemini integration
10. **Notification Module** - Email notifications

### Key Features

- **AI-Powered Interview Questions**: Generates personalized questions based on resume and role
- **Real-time Interview Sessions**: Practice interviews with timer and progress tracking
- **AI Evaluation**: Automated scoring and feedback for answers
- **Skill Gap Analysis**: Identifies areas for improvement
- **Performance Analytics**: Track progress over time
- **Email Notifications**: Welcome, password reset, interview reminders
- **Resume Parsing**: Extract skills and experience using AI

### Technology Stack

#### Backend
- Spring Boot 3.2
- Spring Security + JWT
- PostgreSQL + Liquibase
- Spring Data JPA
- Google Gemini AI
- Spring Mail
- WebFlux (for AI service)
- Swagger/OpenAPI

#### Frontend
- React 18 + TypeScript
- Tailwind CSS
- Framer Motion
- Recharts
- React Router v6
- Vite

## Testing the Application

### 1. Register a New User

1. Navigate to `http://localhost:5173/register`
2. Fill in the registration form
3. Check your email for verification

### 2. Login

1. Navigate to `http://localhost:5173/login`
2. Use your registered credentials

### 3. Upload Resume

1. Go to Profile > Resume
2. Upload a PDF resume
3. AI will parse and extract information

### 4. Start an Interview

1. Click "Start Interview" on dashboard
2. Select role and difficulty
3. Answer AI-generated questions
4. Get instant feedback

### 5. View Analytics

1. Check your dashboard for performance metrics
2. View skill gap analysis
3. Track progress over time

## Environment Variables Summary

### Backend (.env or application-local.yml)
- `DB_USERNAME` - PostgreSQL username
- `DB_PASSWORD` - PostgreSQL password
- `JWT_SECRET` - JWT signing key (256-bit)
- `OPENROUTER_API_KEY` - OpenRouter API key
- `OPENROUTER_MODEL` - Optional model override (defaults to `google/gemini-2.5-flash-lite`)
- `MAIL_USERNAME` - SMTP email
- `MAIL_PASSWORD` - SMTP password

### Frontend (.env)
```
VITE_API_URL=http://localhost:8080
```

## Docker Setup (Optional)

```bash
# Run PostgreSQL
docker-compose up -d postgres

# Run full stack
docker-compose up -d
```

## Troubleshooting

### Backend Issues
- Ensure Java 21 is installed: `java -version`
- Check PostgreSQL is running: `psql -U postgres`
- Verify all environment variables are set

### Frontend Issues
- Clear node_modules: `rm -rf node_modules && npm install`
- Check proxy settings in vite.config.ts

### Database Issues
- Run migrations: `mvn liquibase:update`
- Check connection: `psql -h localhost -U your_user -d interviai_dev`

## Production Deployment

### Backend
1. Build JAR: `mvn clean package`
2. Run: `java -jar target/interviai-backend-1.0.0.jar`

### Frontend
1. Build: `npm run build`
2. Deploy `dist` folder to CDN/static hosting

## API Endpoints

### Authentication
- POST `/api/v1/auth/login`
- POST `/api/v1/auth/logout`
- POST `/api/v1/auth/refresh`
- POST `/api/v1/auth/forgot-password`
- POST `/api/v1/auth/reset-password`

### Users
- POST `/api/v1/users/register`
- GET `/api/v1/users/profile`
- PUT `/api/v1/users/profile`
- POST `/api/v1/users/change-password`

### Interviews
- POST `/api/v1/interviews` - Create interview
- GET `/api/v1/interviews` - List interviews
- POST `/api/v1/interviews/{sessionId}/start` - Start interview
- POST `/api/v1/interviews/answer` - Submit answer
- POST `/api/v1/interviews/{sessionId}/complete` - Complete interview

### Analytics
- GET `/api/v1/analytics/user` - User analytics
- GET `/api/v1/analytics/skill-progress` - Skill progress

### Dashboard
- GET `/api/v1/dashboard` - Dashboard data

## Support

For issues or questions:
- Email: support@interviai.com
- Documentation: [Project Wiki](docs/)
- Issues: [GitHub Issues](https://github.com/interviai/platform/issues)
