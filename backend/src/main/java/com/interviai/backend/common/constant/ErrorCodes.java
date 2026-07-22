package com.interviai.backend.common.constant;

/**
 * Centralized error codes for the application.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
public final class ErrorCodes {

    // Authentication errors (AUTH001-AUTH099)
    public static final String INVALID_CREDENTIALS = "AUTH001";
    public static final String ACCOUNT_DISABLED = "AUTH002";
    public static final String ACCOUNT_LOCKED = "AUTH003";
    public static final String EMAIL_NOT_VERIFIED = "AUTH004";
    public static final String TOKEN_EXPIRED = "AUTH005";
    public static final String INVALID_TOKEN = "AUTH006";
    public static final String TOKEN_REQUIRED = "AUTH007";
    public static final String REFRESH_TOKEN_EXPIRED = "AUTH008";
    public static final String INVALID_REFRESH_TOKEN = "AUTH009";
    public static final String PASSWORD_RESET_REQUIRED = "AUTH010";

    // User errors (USR001-USR099)
    public static final String USER_NOT_FOUND = "USR001";
    public static final String USER_ALREADY_EXISTS = "USR002";
    public static final String EMAIL_ALREADY_EXISTS = "USR003";
    public static final String USERNAME_ALREADY_EXISTS = "USR004";
    public static final String INVALID_USER_DATA = "USR005";
    public static final String USER_CREATION_FAILED = "USR006";
    public static final String USER_UPDATE_FAILED = "USR007";
    public static final String USER_DELETION_FAILED = "USR008";
    public static final String PROFILE_NOT_COMPLETE = "USR009";
    public static final String PREFERENCES_NOT_FOUND = "USR010";

    // Interview errors (INT001-INT099)
    public static final String INTERVIEW_NOT_FOUND = "INT001";
    public static final String INTERVIEW_ALREADY_STARTED = "INT002";
    public static final String INTERVIEW_ALREADY_COMPLETED = "INT003";
    public static final String INTERVIEW_NOT_STARTED = "INT004";
    public static final String INTERVIEW_CANCELLED = "INT005";
    public static final String INVALID_INTERVIEW_STATE = "INT006";
    public static final String INTERVIEW_EXPIRED = "INT007";
    public static final String INSUFFICIENT_QUESTIONS = "INT008";
    public static final String INTERVIEW_CREATION_FAILED = "INT009";
    public static final String SESSION_NOT_FOUND = "INT010";
    public static final String SESSION_EXPIRED = "INT011";

    // Question errors (QST001-QST099)
    public static final String QUESTION_NOT_FOUND = "QST001";
    public static final String QUESTION_GENERATION_FAILED = "QST002";
    public static final String INVALID_QUESTION_TYPE = "QST003";
    public static final String QUESTION_LIMIT_EXCEEDED = "QST004";
    public static final String INSUFFICIENT_RESUME_DATA = "QST005";
    public static final String QUESTION_ALREADY_ANSWERED = "QST006";
    public static final String QUESTION_TIME_EXPIRED = "QST007";
    public static final String INVALID_DIFFICULTY_LEVEL = "QST008";
    public static final String QUESTION_VALIDATION_FAILED = "QST009";
    public static final String QUESTION_UPDATE_FAILED = "QST010";

    // Answer errors (ANS001-ANS099)
    public static final String ANSWER_NOT_FOUND = "ANS001";
    public static final String ANSWER_ALREADY_SUBMITTED = "ANS002";
    public static final String INVALID_ANSWER_FORMAT = "ANS003";
    public static final String ANSWER_TOO_LONG = "ANS004";
    public static final String ANSWER_SUBMISSION_FAILED = "ANS005";
    public static final String ANSWER_UPDATE_NOT_ALLOWED = "ANS006";
    public static final String ANSWER_VALIDATION_FAILED = "ANS007";
    public static final String AUDIO_PROCESSING_FAILED = "ANS008";
    public static final String ANSWER_TIME_LIMIT_EXCEEDED = "ANS009";
    public static final String ANSWER_DELETION_FAILED = "ANS010";

    // Resume errors (RSM001-RSM099)
    public static final String RESUME_NOT_FOUND = "RSM001";
    public static final String RESUME_UPLOAD_FAILED = "RSM002";
    public static final String RESUME_PARSING_FAILED = "RSM003";
    public static final String INVALID_FILE_FORMAT = "RSM004";
    public static final String FILE_TOO_LARGE = "RSM005";
    public static final String FILE_CORRUPTED = "RSM006";
    public static final String RESUME_DELETION_FAILED = "RSM007";
    public static final String RESUME_UPDATE_FAILED = "RSM008";
    public static final String MULTIPLE_RESUMES_NOT_ALLOWED = "RSM009";
    public static final String RESUME_CONTENT_EXTRACTION_FAILED = "RSM010";

    // Evaluation errors (EVL001-EVL099)
    public static final String EVALUATION_NOT_FOUND = "EVL001";
    public static final String EVALUATION_FAILED = "EVL002";
    public static final String EVALUATION_ALREADY_COMPLETED = "EVL003";
    public static final String EVALUATION_IN_PROGRESS = "EVL004";
    public static final String INVALID_EVALUATION_CRITERIA = "EVL005";
    public static final String SCORING_CALCULATION_FAILED = "EVL006";
    public static final String FEEDBACK_GENERATION_FAILED = "EVL007";
    public static final String EVALUATION_UPDATE_FAILED = "EVL008";
    public static final String EVALUATION_DELETION_FAILED = "EVL009";
    public static final String EVALUATION_TIMEOUT = "EVL010";

    // Analytics errors (ANL001-ANL099)
    public static final String ANALYTICS_NOT_FOUND = "ANL001";
    public static final String ANALYTICS_CALCULATION_FAILED = "ANL002";
    public static final String INSUFFICIENT_DATA = "ANL003";
    public static final String ANALYTICS_UPDATE_FAILED = "ANL004";
    public static final String REPORT_GENERATION_FAILED = "ANL005";
    public static final String DASHBOARD_DATA_UNAVAILABLE = "ANL006";
    public static final String METRICS_CALCULATION_FAILED = "ANL007";
    public static final String TREND_ANALYSIS_FAILED = "ANL008";
    public static final String SKILL_ANALYSIS_FAILED = "ANL009";
    public static final String PERFORMANCE_DATA_CORRUPTED = "ANL010";

    // Company & Role errors (CMP001-CMP099)
    public static final String COMPANY_NOT_FOUND = "CMP001";
    public static final String COMPANY_CREATION_FAILED = "CMP002";
    public static final String COMPANY_UPDATE_FAILED = "CMP003";
    public static final String COMPANY_DELETION_FAILED = "CMP004";
    public static final String JOB_ROLE_NOT_FOUND = "CMP005";
    public static final String JOB_ROLE_CREATION_FAILED = "CMP006";
    public static final String JOB_ROLE_UPDATE_FAILED = "CMP007";
    public static final String JOB_ROLE_DELETION_FAILED = "CMP008";
    public static final String INVALID_COMPANY_DATA = "CMP009";
    public static final String INVALID_ROLE_DATA = "CMP010";

    // AI Service errors (AIS001-AIS099)
    public static final String AI_SERVICE_UNAVAILABLE = "AIS001";
    public static final String AI_REQUEST_TIMEOUT = "AIS002";
    public static final String AI_RESPONSE_PARSING_FAILED = "AIS003";
    public static final String AI_RATE_LIMIT_EXCEEDED = "AIS004";
    public static final String AI_QUOTA_EXCEEDED = "AIS005";
    public static final String INVALID_AI_PROMPT = "AIS006";
    public static final String AI_MODEL_ERROR = "AIS007";
    public static final String AI_AUTHENTICATION_FAILED = "AIS008";
    public static final String AI_RESPONSE_VALIDATION_FAILED = "AIS009";
    public static final String AI_SERVICE_CONFIGURATION_ERROR = "AIS010";

    // File Storage errors (FST001-FST099)
    public static final String FILE_STORAGE_UNAVAILABLE = "FST001";
    public static final String FILE_UPLOAD_FAILED = "FST002";
    public static final String FILE_DOWNLOAD_FAILED = "FST003";
    public static final String FILE_DELETION_FAILED = "FST004";
    public static final String STORAGE_QUOTA_EXCEEDED = "FST005";
    public static final String INVALID_FILE_PATH = "FST006";
    public static final String FILE_ACCESS_DENIED = "FST007";
    public static final String FILE_NOT_FOUND = "FST008";
    public static final String STORAGE_CONFIGURATION_ERROR = "FST009";
    public static final String FILE_INTEGRITY_CHECK_FAILED = "FST010";

    // Validation errors (VAL001-VAL099)
    public static final String VALIDATION_ERROR = "VAL001";
    public static final String REQUIRED_FIELD_MISSING = "VAL002";
    public static final String INVALID_FORMAT = "VAL003";
    public static final String VALUE_TOO_SHORT = "VAL004";
    public static final String VALUE_TOO_LONG = "VAL005";
    public static final String VALUE_OUT_OF_RANGE = "VAL006";
    public static final String INVALID_EMAIL_FORMAT = "VAL007";
    public static final String INVALID_PHONE_FORMAT = "VAL008";
    public static final String INVALID_PASSWORD_FORMAT = "VAL009";
    public static final String INVALID_DATE_FORMAT = "VAL010";
    public static final String INVALID_UUID_FORMAT = "VAL011";

    // System errors (SYS001-SYS099)
    public static final String SYSTEM_ERROR = "SYS001";
    public static final String DATABASE_ERROR = "SYS002";
    public static final String DATABASE_CONNECTION_FAILED = "SYS003";
    public static final String TRANSACTION_FAILED = "SYS004";
    public static final String CONFIGURATION_ERROR = "SYS005";
    public static final String SERVICE_UNAVAILABLE = "SYS006";
    public static final String TIMEOUT_ERROR = "SYS007";
    public static final String RESOURCE_EXHAUSTED = "SYS008";
    public static final String CACHE_ERROR = "SYS009";
    public static final String EXTERNAL_SERVICE_ERROR = "SYS010";

    // Permission errors (PRM001-PRM099)
    public static final String ACCESS_DENIED = "PRM001";
    public static final String INSUFFICIENT_PERMISSIONS = "PRM002";
    public static final String RESOURCE_ACCESS_DENIED = "PRM003";
    public static final String OPERATION_NOT_ALLOWED = "PRM004";
    public static final String ADMIN_PRIVILEGES_REQUIRED = "PRM005";
    public static final String OWNERSHIP_REQUIRED = "PRM006";
    public static final String ROLE_REQUIRED = "PRM007";
    public static final String FEATURE_NOT_ENABLED = "PRM008";
    public static final String SUBSCRIPTION_REQUIRED = "PRM009";
    public static final String QUOTA_EXCEEDED = "PRM010";

    // Business Logic errors (BIZ001-BIZ099)
    public static final String BUSINESS_RULE_VIOLATION = "BIZ001";
    public static final String WORKFLOW_VIOLATION = "BIZ002";
    public static final String STATE_TRANSITION_INVALID = "BIZ003";
    public static final String CONSTRAINT_VIOLATION = "BIZ004";
    public static final String DUPLICATE_RESOURCE = "BIZ005";
    public static final String RESOURCE_IN_USE = "BIZ006";
    public static final String OPERATION_NOT_SUPPORTED = "BIZ007";
    public static final String PRECONDITION_FAILED = "BIZ008";
    public static final String POSTCONDITION_FAILED = "BIZ009";
    public static final String INVARIANT_VIOLATION = "BIZ010";

    private ErrorCodes() {
        throw new UnsupportedOperationException("Constants class");
    }
}