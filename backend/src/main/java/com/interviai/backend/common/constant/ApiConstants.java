package com.interviai.backend.common.constant;

/**
 * Constants for API endpoints and common values.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
public final class ApiConstants {

    // API Version
    public static final String API_VERSION_V1 = "/api/v1";

    // Auth endpoints
    public static final String AUTH_BASE_PATH = API_VERSION_V1 + "/auth";
    public static final String LOGIN_ENDPOINT = "/login";
    public static final String LOGOUT_ENDPOINT = "/logout";
    public static final String REFRESH_TOKEN_ENDPOINT = "/refresh";
    public static final String FORGOT_PASSWORD_ENDPOINT = "/forgot-password";
    public static final String RESET_PASSWORD_ENDPOINT = "/reset-password";
    public static final String VERIFY_EMAIL_ENDPOINT = "/verify-email";

    // User endpoints
    public static final String USERS_BASE_PATH = API_VERSION_V1 + "/users";
    public static final String REGISTER_ENDPOINT = "/register";
    public static final String PROFILE_ENDPOINT = "/profile";
    public static final String PREFERENCES_ENDPOINT = "/preferences";

    // Resume endpoints
    public static final String RESUMES_BASE_PATH = API_VERSION_V1 + "/resumes";
    public static final String UPLOAD_ENDPOINT = "/upload";
    public static final String PARSE_ENDPOINT = "/parse";

    // Interview endpoints
    public static final String INTERVIEWS_BASE_PATH = API_VERSION_V1 + "/interviews";
    public static final String START_ENDPOINT = "/start";
    public static final String SUBMIT_ENDPOINT = "/submit";
    public static final String COMPLETE_ENDPOINT = "/complete";
    public static final String CANCEL_ENDPOINT = "/cancel";

    // Question endpoints
    public static final String QUESTIONS_BASE_PATH = API_VERSION_V1 + "/questions";
    public static final String GENERATE_ENDPOINT = "/generate";

    // Answer endpoints
    public static final String ANSWERS_BASE_PATH = API_VERSION_V1 + "/answers";

    // Evaluation endpoints
    public static final String EVALUATIONS_BASE_PATH = API_VERSION_V1 + "/evaluations";

    // Analytics endpoints
    public static final String ANALYTICS_BASE_PATH = API_VERSION_V1 + "/analytics";
    public static final String DASHBOARD_ENDPOINT = "/dashboard";
    public static final String REPORTS_ENDPOINT = "/reports";

    // Admin endpoints
    public static final String ADMIN_BASE_PATH = API_VERSION_V1 + "/admin";
    public static final String SYSTEM_ENDPOINT = "/system";
    public static final String CONFIGURATION_ENDPOINT = "/configuration";

    // Common path variables
    public static final String ID_PATH_VARIABLE = "/{id}";
    public static final String USER_ID_PATH_VARIABLE = "/{userId}";
    public static final String INTERVIEW_ID_PATH_VARIABLE = "/{interviewId}";

    // Request/Response headers
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";

    // Pagination defaults
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_DIRECTION = "ASC";
    public static final String DEFAULT_SORT_PROPERTY = "createdAt";

    // File upload constraints
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final String[] ALLOWED_FILE_EXTENSIONS = {"pdf", "doc", "docx"};
    public static final String[] ALLOWED_IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif"};

    // Security constraints
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 100;
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int ACCOUNT_LOCKOUT_MINUTES = 30;

    // JWT token constraints
    public static final long ACCESS_TOKEN_VALIDITY_SECONDS = 3600; // 1 hour
    public static final long REFRESH_TOKEN_VALIDITY_SECONDS = 2592000; // 30 days

    // Interview constraints
    public static final int MIN_INTERVIEW_DURATION_MINUTES = 15;
    public static final int MAX_INTERVIEW_DURATION_MINUTES = 180;
    public static final int MIN_QUESTIONS_PER_INTERVIEW = 5;
    public static final int MAX_QUESTIONS_PER_INTERVIEW = 50;
    public static final int MAX_ANSWER_LENGTH = 5000;

    // AI service constraints
    public static final int MAX_AI_PROMPT_LENGTH = 10000;
    public static final int AI_REQUEST_TIMEOUT_SECONDS = 30;
    public static final int MAX_AI_RETRY_ATTEMPTS = 3;

    // Cache keys
    public static final String USER_CACHE_KEY = "user:";
    public static final String INTERVIEW_CACHE_KEY = "interview:";
    public static final String ANALYTICS_CACHE_KEY = "analytics:";
    public static final int CACHE_TTL_MINUTES = 60;

    // Rate limiting
    public static final int RATE_LIMIT_PER_MINUTE = 60;
    public static final int LOGIN_RATE_LIMIT_PER_MINUTE = 5;
    public static final int FILE_UPLOAD_RATE_LIMIT_PER_MINUTE = 10;

    // Validation messages
    public static final String REQUIRED_FIELD_MESSAGE = " is required";
    public static final String INVALID_FORMAT_MESSAGE = " has invalid format";
    public static final String TOO_SHORT_MESSAGE = " is too short";
    public static final String TOO_LONG_MESSAGE = " is too long";
    public static final String OUT_OF_RANGE_MESSAGE = " is out of valid range";

    // Success messages
    public static final String LOGIN_SUCCESS_MESSAGE = "Login successful";
    public static final String LOGOUT_SUCCESS_MESSAGE = "Logout successful";
    public static final String REGISTRATION_SUCCESS_MESSAGE = "Registration successful";
    public static final String PROFILE_UPDATE_SUCCESS_MESSAGE = "Profile updated successfully";
    public static final String PASSWORD_RESET_SUCCESS_MESSAGE = "Password reset email sent";
    public static final String EMAIL_VERIFICATION_SUCCESS_MESSAGE = "Email verified successfully";

    // Error messages
    public static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";
    public static final String ACCOUNT_DISABLED_MESSAGE = "Account is disabled";
    public static final String EMAIL_NOT_VERIFIED_MESSAGE = "Email verification required";
    public static final String TOKEN_EXPIRED_MESSAGE = "Token has expired";
    public static final String RESOURCE_NOT_FOUND_MESSAGE = "Resource not found";
    public static final String ACCESS_DENIED_MESSAGE = "Access denied";
    public static final String INTERNAL_ERROR_MESSAGE = "Internal server error";

    private ApiConstants() {
        throw new UnsupportedOperationException("Constants class");
    }
}