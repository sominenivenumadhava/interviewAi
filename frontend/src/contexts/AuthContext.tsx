import React, { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import apiClient, { API_ENDPOINTS, ApiError, ApiFieldError } from '../lib/apiClient';

// Types for our authentication context
export interface User {
  id: string;
  email: string;
  username?: string;
  firstName: string;
  lastName: string;
  phone?: string;
  emailVerified: boolean;
  profilePictureUrl?: string;
  bio?: string;
  timezone: string;
  language: string;
  role: 'USER' | 'MODERATOR' | 'ADMIN';
  marketingEmailsEnabled: boolean;
  notificationEmailsEnabled: boolean;
  createdAt: string;
  lastLoginAt?: string;
  profileCompleteness: number;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresAt: number;
}

export interface LoginResponse {
  user: User;
  tokens: AuthTokens;
}

export interface RegisterRequest {
  email: string;
  password: string;
  confirmPassword: string;
  firstName: string;
  lastName: string;
  phone?: string;
  acceptTerms: boolean;
  acceptPrivacy: boolean;
}

export interface RegisterError {
  message: string;
  fieldErrors?: ApiFieldError[];
}

export interface LoginRequest {
  email: string;
  password: string;
}

interface AuthContextType {
  // State
  user: User | null;
  tokens: AuthTokens | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  
  // Actions
  login: (credentials: LoginRequest) => Promise<void>;
  register: (userData: RegisterRequest) => Promise<void>;
  logout: () => void;
  refreshToken: () => Promise<boolean>;
  updateUser: (updates: Partial<User>) => void;
  
  // Utility
  hasRole: (role: 'USER' | 'MODERATOR' | 'ADMIN') => boolean;
  isEmailVerified: () => boolean;
}

// Create context
const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Local storage keys
const STORAGE_KEYS = {
  ACCESS_TOKEN: 'interviai_access_token',
  REFRESH_TOKEN: 'interviai_refresh_token',
  USER: 'interviai_user',
} as const;

// AuthProvider component
interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [tokens, setTokens] = useState<AuthTokens | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Initialize auth state from localStorage on mount
  useEffect(() => {
    initializeAuth();
  }, []);

  // Initialize authentication state from stored data
  const initializeAuth = async () => {
    try {
      const storedAccessToken = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
      const storedRefreshToken = localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN);
      const storedUser = localStorage.getItem(STORAGE_KEYS.USER);

      if (storedAccessToken && storedRefreshToken && storedUser) {
        const parsedUser = JSON.parse(storedUser) as User;
        
        // Check if access token is expired
        const tokenData = parseJWT(storedAccessToken);
        const isExpired = tokenData.exp * 1000 <= Date.now();

        if (isExpired) {
          // Try to refresh the token
          const refreshed = await performTokenRefresh(storedRefreshToken);
          if (!refreshed) {
            // Refresh failed, clear all data
            clearAuthData();
            setIsLoading(false);
            return;
          }
        } else {
          // Token is still valid
          setTokens({
            accessToken: storedAccessToken,
            refreshToken: storedRefreshToken,
            expiresAt: tokenData.exp * 1000,
          });
        }

        setUser(parsedUser);
      }
    } catch (error) {
      console.error('Error initializing auth:', error);
      clearAuthData();
    } finally {
      setIsLoading(false);
    }
  };

  // Login function
  const login = async (credentials: LoginRequest): Promise<void> => {
    setIsLoading(true);
    
    try {
      const response = await apiClient.post(API_ENDPOINTS.AUTH.LOGIN, credentials, {
        skipAuth: true,
      });

      if (!response.data) {
        throw new Error('Invalid response from server');
      }

      // Create tokens object from flat backend response
      const tokenData: AuthTokens = {
        accessToken: response.data.accessToken,
        refreshToken: response.data.refreshToken,
        expiresAt: Date.now() + (response.data.expiresIn || 3600) * 1000,
      };

      // Store tokens and user data
      storeAuthData(response.data.user, tokenData);
      setUser(response.data.user);
      setTokens(tokenData);
      
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  // Register function
  const register = async (userData: RegisterRequest): Promise<void> => {
    setIsLoading(true);

    try {
      console.debug('[AuthContext] Registering user:', userData.email);

      const response = await apiClient.post(API_ENDPOINTS.USER.REGISTER, userData, {
        skipAuth: true,
      });

      console.debug('[AuthContext] Registration response:', response);

      // Registration successful — the backend returns the created user.
      // We do NOT auto-login here to keep the flow simple and robust.
      // The caller (Register.tsx) will redirect to /login.
      // (If the backend ever returns tokens, handle them here.)

    } catch (error) {
      console.error('[AuthContext] Registration error:', error);
      // Re-throw so Register.tsx can display the message
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  // Logout function
  const logout = (): void => {
    try {
      // Call logout endpoint if we have a refresh token
      if (tokens?.refreshToken) {
        apiClient.post(API_ENDPOINTS.AUTH.LOGOUT, { 
          refreshToken: tokens.refreshToken 
        }).catch(() => {
          // Ignore errors on logout - we're clearing local state anyway
        });
      }
    } finally {
      // Always clear local state
      clearAuthData();
      setUser(null);
      setTokens(null);
    }
  };

  // Refresh token function
  const refreshToken = async (): Promise<boolean> => {
    if (!tokens?.refreshToken) {
      return false;
    }

    return await performTokenRefresh(tokens.refreshToken);
  };

  // Perform token refresh
  const performTokenRefresh = async (refreshTokenValue: string): Promise<boolean> => {
    try {
      const response = await apiClient.post(API_ENDPOINTS.AUTH.REFRESH, {
        refreshToken: refreshTokenValue,
      }, {
        skipAuth: true,
        retry: false,
      });

      if (!response.data) {
        return false;
      }

      const newTokens: AuthTokens = {
        accessToken: response.data.accessToken,
        refreshToken: response.data.refreshToken,
        expiresAt: parseJWT(response.data.accessToken).exp * 1000,
      };

      // Update stored tokens
      localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, newTokens.accessToken);
      localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, newTokens.refreshToken);
      
      setTokens(newTokens);
      
      return true;
    } catch (error) {
      console.error('Token refresh error:', error);
      return false;
    }
  };

  // Update user data
  const updateUser = (updates: Partial<User>): void => {
    if (!user) return;
    
    const updatedUser = { ...user, ...updates };
    setUser(updatedUser);
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(updatedUser));
  };

  // Utility functions
  const hasRole = (role: 'USER' | 'MODERATOR' | 'ADMIN'): boolean => {
    if (!user) return false;
    
    const roleHierarchy = { 'USER': 1, 'MODERATOR': 2, 'ADMIN': 3 };
    return roleHierarchy[user.role] >= roleHierarchy[role];
  };

  const isEmailVerified = (): boolean => {
    return user?.emailVerified ?? false;
  };

  // Helper functions
  const storeAuthData = (userData: User, tokenData: AuthTokens): void => {
    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, tokenData.accessToken);
    localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, tokenData.refreshToken);
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(userData));
  };

  const clearAuthData = (): void => {
    localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER);
  };

  const parseJWT = (token: string): any => {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('Error parsing JWT:', error);
      return {};
    }
  };

  const isAuthenticated = !!user && !!tokens;

  const contextValue: AuthContextType = {
    user,
    tokens,
    isLoading,
    isAuthenticated,
    login,
    register,
    logout,
    refreshToken,
    updateUser,
    hasRole,
    isEmailVerified,
  };

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
};

// Hook to use the auth context
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

// Hook for protected routes
export const useRequireAuth = (requiredRole?: 'USER' | 'MODERATOR' | 'ADMIN') => {
  const auth = useAuth();
  
  useEffect(() => {
    if (!auth.isLoading) {
      if (!auth.isAuthenticated) {
        // Redirect to login page
        window.location.href = '/login';
        return;
      }
      
      if (requiredRole && !auth.hasRole(requiredRole)) {
        // Redirect to unauthorized page or home
        window.location.href = '/';
        return;
      }
    }
  }, [auth.isLoading, auth.isAuthenticated, auth.hasRole, requiredRole]);
  
  return auth;
};