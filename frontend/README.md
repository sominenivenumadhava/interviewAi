# InterviAI Frontend

## Overview

The frontend for InterviAI is built with React 18, TypeScript, and Vite, providing a modern, responsive user interface for AI-powered interview preparation.

## Technology Stack

- **Framework**: React 18 with TypeScript
- **Build Tool**: Vite for fast development and building
- **Styling**: Tailwind CSS for utility-first styling
- **State Management**: React Context API + React Hooks
- **HTTP Client**: Native Fetch API
- **Form Handling**: Custom hooks and validation
- **Routing**: React Router v6
- **Icons**: Lucide React icons

## Project Structure

```
src/
├── components/
│   ├── layout/
│   │   ├── AppLayout.tsx        # Main application layout
│   │   └── TopNav.tsx           # Navigation component
│   └── ui/
│       ├── Badge.tsx            # Reusable badge component
│       ├── Button.tsx           # Button component
│       ├── Card.tsx             # Card component
│       └── Input.tsx            # Input component
├── contexts/
│   └── ThemeContext.tsx         # Theme management
├── data/
│   └── mockData.ts              # Mock data for development
├── lib/
│   └── utils.ts                 # Utility functions
├── pages/
│   ├── auth/
│   │   ├── Login.tsx            # Login page
│   │   └── Register.tsx         # Registration page
│   ├── interview/
│   │   ├── CompanySelect.tsx    # Company selection
│   │   ├── Config.tsx           # Interview configuration
│   │   ├── RoleSelect.tsx       # Role selection
│   │   └── Room.tsx             # Interview room
│   ├── Admin.tsx                # Admin dashboard
│   ├── Analytics.tsx            # Analytics page
│   ├── Dashboard.tsx            # Main dashboard
│   ├── Evaluation.tsx           # Evaluation results
│   ├── History.tsx              # Interview history
│   ├── Landing.tsx              # Landing page
│   ├── Profile.tsx              # User profile
│   ├── ResumeUpload.tsx         # Resume upload
│   ├── Roadmap.tsx              # Learning roadmap
│   └── SkillGap.tsx             # Skill gap analysis
├── App.tsx                      # Main application component
├── index.css                    # Global styles
└── index.tsx                    # Application entry point
```

## Getting Started

### Prerequisites

- Node.js 18+ 
- npm or yarn package manager

### Installation

```bash
# Install dependencies
npm install

# Start development server
npm run dev
```

The application will be available at `http://localhost:5173`

## Available Scripts

```bash
# Development
npm run dev          # Start development server with hot reload
npm run build        # Build for production
npm run preview      # Preview production build locally

# Code Quality
npm run lint         # Run ESLint
npm run type-check   # Run TypeScript compiler check

# Testing (when implemented)
npm test             # Run unit tests
npm run test:e2e     # Run end-to-end tests
```

## Environment Configuration

### Development
Create a `.env.local` file:
```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_APP_NAME=InterviAI
VITE_APP_VERSION=1.0.0
```

### Production
```env
VITE_API_BASE_URL=https://api.interviai.com/api/v1
VITE_APP_NAME=InterviAI
VITE_APP_VERSION=1.0.0
```

## Component Guidelines

### UI Components

All UI components follow these patterns:

```typescript
// components/ui/Button.tsx
interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  loading?: boolean;
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  loading = false,
  children,
  className,
  disabled,
  ...props
}) => {
  // Component implementation
};
```

### Page Components

Page components are organized by feature:

```typescript
// pages/Dashboard.tsx
import { useAuth } from '../contexts/AuthContext';
import { useApiCall } from '../hooks/useApiCall';

export const Dashboard: React.FC = () => {
  const { user } = useAuth();
  const { data, loading, error } = useApiCall('/dashboard');
  
  // Page implementation
};
```

## State Management

### Context API Structure

```typescript
// contexts/AuthContext.tsx
interface AuthContextType {
  user: User | null;
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => void;
  loading: boolean;
  error: string | null;
}

export const AuthProvider: React.FC<{ children: ReactNode }> = ({
  children
}) => {
  // Context implementation
};
```

### Custom Hooks

```typescript
// hooks/useApiCall.ts
export const useApiCall = <T>(endpoint: string, options?: RequestInit) => {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Hook implementation
  
  return { data, loading, error, refetch };
};
```

## Styling Guidelines

### Tailwind CSS Usage

```typescript
// Use consistent spacing and responsive design
<div className="p-4 md:p-6 lg:p-8 space-y-4">
  <h1 className="text-2xl md:text-3xl font-bold text-gray-900 dark:text-white">
    Dashboard
  </h1>
  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
    {/* Grid content */}
  </div>
</div>
```

### Custom CSS (when needed)

```css
/* index.css - Keep minimal global styles */
@tailwind base;
@tailwind components;
@tailwind utilities;

@layer base {
  html {
    scroll-behavior: smooth;
  }
}

@layer components {
  .btn-primary {
    @apply bg-blue-600 hover:bg-blue-700 text-white font-medium px-4 py-2 rounded-lg transition-colors;
  }
}
```

## API Integration

### HTTP Client Setup

```typescript
// lib/api.ts
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

class ApiClient {
  private async request<T>(
    endpoint: string, 
    options?: RequestInit
  ): Promise<T> {
    const token = localStorage.getItem('accessToken');
    
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      headers: {
        'Content-Type': 'application/json',
        ...(token && { Authorization: `Bearer ${token}` }),
        ...options?.headers,
      },
      ...options,
    });

    if (!response.ok) {
      throw new Error(`API Error: ${response.status}`);
    }

    return response.json();
  }

  get<T>(endpoint: string) {
    return this.request<T>(endpoint);
  }

  post<T>(endpoint: string, data?: any) {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }
}

export const apiClient = new ApiClient();
```

## Routing

```typescript
// App.tsx routing setup
import { BrowserRouter, Routes, Route } from 'react-router-dom';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        
        {/* Protected routes */}
        <Route element={<ProtectedRoute />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/interviews/:id" element={<InterviewRoom />} />
        </Route>
        
        {/* Admin routes */}
        <Route element={<AdminRoute />}>
          <Route path="/admin" element={<Admin />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
```

## Performance Optimization

### Code Splitting

```typescript
// Lazy load pages for better performance
const Dashboard = lazy(() => import('./pages/Dashboard'));
const Profile = lazy(() => import('./pages/Profile'));

function App() {
  return (
    <Suspense fallback={<LoadingSpinner />}>
      <Routes>
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/profile" element={<Profile />} />
      </Routes>
    </Suspense>
  );
}
```

### Image Optimization

```typescript
// Use proper image formats and lazy loading
<img 
  src={`/images/${imageName}.webp`}
  alt="Description"
  loading="lazy"
  className="w-full h-auto object-cover"
/>
```

## Testing Strategy

### Unit Tests (Planned)

```typescript
// __tests__/components/Button.test.tsx
import { render, screen, fireEvent } from '@testing-library/react';
import { Button } from '../components/ui/Button';

describe('Button', () => {
  it('renders correctly', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByText('Click me')).toBeInTheDocument();
  });

  it('handles click events', () => {
    const handleClick = jest.fn();
    render(<Button onClick={handleClick}>Click me</Button>);
    
    fireEvent.click(screen.getByText('Click me'));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });
});
```

### E2E Tests (Planned)

```typescript
// cypress/e2e/auth.cy.ts
describe('Authentication', () => {
  it('should login successfully', () => {
    cy.visit('/login');
    cy.get('[data-cy="email"]').type('user@example.com');
    cy.get('[data-cy="password"]').type('password123');
    cy.get('[data-cy="submit"]').click();
    
    cy.url().should('include', '/dashboard');
    cy.contains('Welcome').should('be.visible');
  });
});
```

## Deployment

### Build Configuration

```typescript
// vite.config.ts
export default defineConfig({
  plugins: [react()],
  build: {
    outDir: 'dist',
    sourcemap: process.env.NODE_ENV === 'development',
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom'],
          router: ['react-router-dom'],
        },
      },
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```

### Production Build

```bash
# Build for production
npm run build

# Preview production build
npm run preview

# Deploy (example with Netlify)
npm run build && netlify deploy --prod --dir=dist
```

## Browser Support

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Contributing

1. Follow the existing code style and component patterns
2. Add TypeScript types for all props and functions
3. Write tests for new components and features
4. Use semantic commit messages
5. Update documentation for new features

## Troubleshooting

### Common Issues

1. **Vite Dev Server Issues**
   ```bash
   # Clear Vite cache
   rm -rf node_modules/.vite
   npm run dev
   ```

2. **TypeScript Errors**
   ```bash
   # Run type checking
   npm run type-check
   ```

3. **Build Failures**
   ```bash
   # Clean install
   rm -rf node_modules package-lock.json
   npm install
   npm run build
   ```

### Development Tips

- Use React DevTools for debugging
- Enable Vite's HMR for fast development
- Use TypeScript strict mode for better type safety
- Implement error boundaries for production resilience

---

This frontend provides a solid foundation for the InterviAI platform with modern React practices, TypeScript safety, and a component-driven architecture that's maintainable and scalable.