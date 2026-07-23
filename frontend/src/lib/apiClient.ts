/**
 * HTTP API client with automatic token management and request/response interceptors.
 * Handles authentication, retry logic, and error handling.
 */

export interface ApiFieldError {
  field: string;
  message: string;
  rejectedValue?: any;
}

export interface ApiResponse<T = any> {
  data?: T;
  message?: string;
  success?: boolean;
  errors?: string[];
  fieldErrors?: ApiFieldError[];
  details?: ApiFieldError[]; // Spring Boot ErrorResponse uses 'details' for validation errors
  meta?: {
    page?: number;
    limit?: number;
    total?: number;
    totalPages?: number;
  };
}

export interface RequestConfig extends RequestInit {
  // Custom options
  skipAuth?: boolean;
  retry?: boolean;
  retryCount?: number;
  timeout?: number;
}

const AUTH_REFRESH_ENDPOINT = '/api/v1/auth/refresh';

class ApiClient {
  private baseURL: string;
  private defaultTimeout: number = 15000; // 15 seconds
  private maxRetries: number = 1; // only retry once on 5xx
  private retryDelay: number = 1000;
  private refreshRequest: Promise<boolean> | null = null;

  constructor() {
    // In development: use empty string so all /api/* calls go through Vite's proxy to port 8082.
    // In production: read VITE_API_BASE_URL from environment.
    if (import.meta.env.PROD) {
      this.baseURL = import.meta.env.VITE_API_BASE_URL || 'https://api.interviai.com';
    } else {
      // Empty base URL = relative paths → Vite proxy handles CORS automatically
      this.baseURL = '';
    }
    console.debug(`[ApiClient] Initialized. Base URL: "${this.baseURL || '(relative — Vite proxy)'}"`);
  }

  /**
   * Make an HTTP request with automatic token handling and error management.
   */
  async request<T = any>(
    endpoint: string,
    config: RequestConfig = {}
  ): Promise<ApiResponse<T>> {
    const {
      skipAuth = false,
      retry = true,
      retryCount = 0,
      timeout = this.defaultTimeout,
      ...fetchConfig
    } = config;

    const url = `${this.baseURL}${endpoint}`;

    // If another request is already refreshing an expired/missing access token,
    // protected requests must wait for it instead of being sent anonymously.
    if (!skipAuth && endpoint !== AUTH_REFRESH_ENDPOINT && !this.getAccessToken()) {
      const refreshed = await this.handleTokenRefresh();
      if (!refreshed || !this.getAccessToken()) {
        this.handleAuthFailure();
        throw new ApiClientError(
          'Your session has expired. Please log in again.',
          'AUTH_FAILED',
          401
        );
      }
    }

    // Build headers
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(fetchConfig.headers as Record<string, string>),
    };

    // Attach JWT if available and not skipped
    if (!skipAuth) {
      const token = this.getAccessToken();
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
    }

    // Debug logging — log every request
    console.debug(`[ApiClient] ▶ ${fetchConfig.method || 'GET'} ${url}`);
    if (fetchConfig.body) {
      try {
        console.debug('[ApiClient]   Payload:', JSON.parse(fetchConfig.body as string));
      } catch {
        console.debug('[ApiClient]   Body (raw):', fetchConfig.body);
      }
    }

    // Abort controller for timeout
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeout);

    try {
      const response = await fetch(url, {
        ...fetchConfig,
        headers,
        signal: controller.signal,
      });

      clearTimeout(timeoutId);
      console.debug(`[ApiClient] ◀ ${response.status} ${response.statusText} — ${url}`);

      // Handle 401 Unauthorized — try token refresh
      if (response.status === 401) {
        // Public/auth requests (especially refresh itself) must never recursively
        // trigger another refresh attempt.
        const canRefresh = !skipAuth && endpoint !== AUTH_REFRESH_ENDPOINT;
        if (canRefresh) {
          const refreshed = await this.handleTokenRefresh();
          if (refreshed && retryCount < this.maxRetries) {
            return this.request<T>(endpoint, { ...config, retryCount: retryCount + 1 });
          }
        }

        this.handleAuthFailure();
        throw new ApiClientError('Your session has expired. Please log in again.', 'AUTH_FAILED', 401);
      }

      // Handle error responses (4xx, 5xx)
      if (!response.ok) {
        let errorMessage = `Request failed (HTTP ${response.status})`;
        let errorDetails: Record<string, any> = {};
        let fieldErrors: ApiFieldError[] = [];

        try {
          const errorData = await response.json();
          console.debug('[ApiClient]   Error body:', errorData);

          // Spring Boot GlobalExceptionHandler format: { message, fieldErrors: [{field, message}] }
          if (errorData.message) {
            errorMessage = errorData.message;
          }

          // Extract field-level validation errors
          // Spring Boot ErrorResponse uses 'details' array (GlobalExceptionHandler.java)
          const rawFieldErrors = errorData.details || errorData.fieldErrors || [];
          if (Array.isArray(rawFieldErrors)) {
            fieldErrors = rawFieldErrors.map((fe: any) => ({
              field: fe.field || fe.propertyPath || '',
              message: fe.message || fe.defaultMessage || String(fe),
              rejectedValue: fe.rejectedValue,
            }));
            // If there are field errors, build a combined message
            if (fieldErrors.length > 0 && !errorData.message) {
              errorMessage = fieldErrors.map(fe => `${fe.field}: ${fe.message}`).join('; ');
            }
          }

          errorDetails = errorData;
        } catch {
          errorMessage = response.statusText || errorMessage;
          console.debug('[ApiClient]   (Could not parse error response as JSON)');
        }

        // Only retry on server errors (5xx), never on client errors (4xx)
        if (response.status >= 500 && retry && retryCount < this.maxRetries) {
          console.warn(`[ApiClient] Server error ${response.status}, retrying (${retryCount + 1}/${this.maxRetries})…`);
          await this.delay(this.retryDelay * (retryCount + 1));
          return this.request<T>(endpoint, { ...config, retryCount: retryCount + 1 });
        }

        const err = new ApiClientError(errorMessage, 'REQUEST_FAILED', response.status, errorDetails);
        err.fieldErrors = fieldErrors;
        // Also expose raw details for consumers that check errorData.details
        (err as any).details = errorDetails.details || errorDetails.fieldErrors || fieldErrors;
        throw err;
      }

      // Parse successful response
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        const data = await response.json();
        console.debug('[ApiClient]   Response data:', data);
        return data;
      } else {
        return { data: await response.text() as T, success: true };
      }

    } catch (error) {
      clearTimeout(timeoutId);

      // Re-throw our own errors unchanged
      if (error instanceof ApiClientError) {
        throw error;
      }

      // Timeout
      if (error instanceof DOMException && error.name === 'AbortError') {
        console.error('[ApiClient] ✗ Timeout:', url);
        throw new ApiClientError(
          `Request timed out after ${timeout / 1000} seconds. ` +
          `Ensure the backend is running on port 8082.`,
          'TIMEOUT',
          408
        );
      }

      // Network / connection refused
      const isNetworkError = error instanceof TypeError &&
        (error.message.includes('Failed to fetch') ||
         error.message.includes('NetworkError') ||
         error.message.includes('Load failed'));

      console.error('[ApiClient] ✗ Network error:', error);

      if (isNetworkError) {
        // For auth/register, fail immediately with clear message — no retries
        if (endpoint.includes('/register') || endpoint.includes('/login') || endpoint.includes('/auth')) {
          throw new ApiClientError(
            'Cannot connect to the server. Make sure the Spring Boot backend is running on port 8082.',
            'CONNECTION_REFUSED'
          );
        }

        if (retry && retryCount < this.maxRetries) {
          await this.delay(this.retryDelay * (retryCount + 1));
          return this.request<T>(endpoint, { ...config, retryCount: retryCount + 1 });
        }
      }

      throw new ApiClientError(
        error instanceof Error ? error.message : 'Unexpected network error occurred.',
        'NETWORK_ERROR'
      );
    }
  }

  async get<T = any>(endpoint: string, config?: RequestConfig): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, { method: 'GET', ...config });
  }

  async post<T = any>(endpoint: string, data?: any, config?: RequestConfig): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
      ...config,
    });
  }

  async put<T = any>(endpoint: string, data?: any, config?: RequestConfig): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
      ...config,
    });
  }

  async patch<T = any>(endpoint: string, data?: any, config?: RequestConfig): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, {
      method: 'PATCH',
      body: data ? JSON.stringify(data) : undefined,
      ...config,
    });
  }

  async delete<T = any>(endpoint: string, config?: RequestConfig): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, { method: 'DELETE', ...config });
  }

  async upload<T = any>(endpoint: string, file: File | FormData, config?: RequestConfig): Promise<ApiResponse<T>> {
    const formData = file instanceof File ? new FormData() : file;
    if (file instanceof File) formData.append('file', file);
    const { headers, ...restConfig } = config || {};
    return this.request<T>(endpoint, {
      method: 'POST',
      body: formData,
      headers: { ...(headers as Record<string, string>) },
      ...restConfig,
    });
  }

  private getAccessToken(): string | null {
    return localStorage.getItem('interviai_access_token');
  }

  private getRefreshToken(): string | null {
    return localStorage.getItem('interviai_refresh_token');
  }

  private async handleTokenRefresh(): Promise<boolean> {
    // Reuse one in-flight refresh when several protected requests receive 401
    // at the same time. This avoids refresh races and repeated backend calls.
    if (this.refreshRequest) {
      return this.refreshRequest;
    }

    const attempt = this.performTokenRefreshRequest();
    this.refreshRequest = attempt;

    try {
      return await attempt;
    } finally {
      if (this.refreshRequest === attempt) {
        this.refreshRequest = null;
      }
    }
  }

  private async performTokenRefreshRequest(): Promise<boolean> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) return false;

    try {
      const response = await this.request(AUTH_REFRESH_ENDPOINT, {
        method: 'POST',
        skipAuth: true,
        retry: false,
        body: JSON.stringify({ refreshToken }),
      });

      if (response.data) {
        localStorage.setItem('interviai_access_token', response.data.accessToken);
        localStorage.setItem(
          'interviai_refresh_token',
          response.data.refreshToken || refreshToken
        );
        return true;
      }
      return false;
    } catch (error) {
      console.error('[ApiClient] Token refresh failed:', error);
      return false;
    }
  }

  private handleAuthFailure(): void {
    localStorage.removeItem('interviai_access_token');
    localStorage.removeItem('interviai_refresh_token');
    localStorage.removeItem('interviai_user');
    if (!window.location.pathname.includes('/login')) {
      window.location.href = '/login';
    }
  }

  private delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }

  setBaseURL(url: string): void {
    this.baseURL = url;
  }

  getBaseURL(): string {
    return this.baseURL;
  }
}

// Custom error class — carries field-level validation errors from Spring Boot
class ApiClientError extends Error {
  public fieldErrors?: ApiFieldError[];

  constructor(
    message: string,
    public code?: string,
    public status?: number,
    public details?: Record<string, any>
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

// Export singleton instance
const apiClient = new ApiClient();
export default apiClient;

// Export types and error class
export { ApiClientError as ApiError };

// Common API endpoints as constants
export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: '/api/v1/auth/login',
    LOGOUT: '/api/v1/auth/logout',
    REFRESH: '/api/v1/auth/refresh',
    FORGOT_PASSWORD: '/api/v1/auth/forgot-password',
    RESET_PASSWORD: '/api/v1/auth/reset-password',
  },
  USER: {
    REGISTER: '/api/v1/users/register',
    PROFILE: '/api/v1/users/profile',
    UPDATE_PROFILE: '/api/v1/users/profile',
    CHANGE_PASSWORD: '/api/v1/users/change-password',
    VERIFY_EMAIL: '/api/v1/users/verify-email',
    PREFERENCES: '/api/v1/users/preferences',
  },
  INTERVIEW: {
    CREATE: '/api/v1/interviews',
    LIST: '/api/v1/interviews',
    GET: (id: string) => `/api/v1/interviews/${id}`,
    SESSION: (sessionId: string) => `/api/v1/interviews/session/${sessionId}`,
    START: (sessionId: string) => `/api/v1/interviews/${sessionId}/start`,
    COMPLETE: (sessionId: string) => `/api/v1/interviews/${sessionId}/complete`,
    PAUSE: (sessionId: string) => `/api/v1/interviews/${sessionId}/pause`,
    RESUME: (sessionId: string) => `/api/v1/interviews/${sessionId}/resume`,
    CANCEL: (sessionId: string) => `/api/v1/interviews/${sessionId}/cancel`,
    SUBMIT_ANSWER: '/api/v1/interviews/answer',
    QUESTIONS: (sessionId: string) => `/api/v1/interviews/${sessionId}/questions`,
    NEXT_QUESTION: (sessionId: string) => `/api/v1/interviews/${sessionId}/next-question`,
    EVALUATE: (sessionId: string, questionOrder: number) =>
      `/api/v1/interviews/${sessionId}/questions/${questionOrder}/evaluate`,
    EVALUATE_ALL: (sessionId: string) => `/api/v1/interviews/${sessionId}/evaluate-all`,
  },
  RESUME: {
    UPLOAD: '/api/v1/resumes/upload',
    LIST: '/api/v1/resumes',
    GET: (id: string) => `/api/v1/resumes/${id}`,
    UPDATE: (id: string) => `/api/v1/resumes/${id}`,
    DELETE: (id: string) => `/api/v1/resumes/${id}`,
    SET_PRIMARY: (id: string) => `/api/v1/resumes/${id}/set-primary`,
    DOWNLOAD: (id: string) => `/api/v1/resumes/${id}/download`,
  },
  EVALUATION: {
    INTERVIEW: (sessionId: string) => `/api/v1/evaluations/interview/${sessionId}`,
    SKILL_GAP: '/api/v1/evaluations/skill-gap',
    PERFORMANCE_TRENDS: '/api/v1/evaluations/performance-trends',
    BENCHMARK: (role: string) => `/api/v1/evaluations/benchmark/${role}`,
    PRACTICE_QUESTIONS: '/api/v1/evaluations/practice-questions',
  },
  ANALYTICS: {
    USER: '/api/v1/analytics/user',
    COMPANY: (company: string) => `/api/v1/analytics/company/${company}`,
    ROLE_COMPARISON: '/api/v1/analytics/role-comparison',
    SKILL_PROGRESS: '/api/v1/analytics/skill-progress',
    PREPARATION: (role: string) => `/api/v1/analytics/preparation-insights/${role}`,
    EXPORT: '/api/v1/analytics/export',
  },
  DASHBOARD: {
    GET: '/api/v1/dashboard',
    REFRESH: '/api/v1/dashboard/refresh',
  },
  SPEECH: {
    TOKEN: '/api/v1/speech/token',
    VALIDATE: '/api/v1/speech/validate',
  },
} as const;
