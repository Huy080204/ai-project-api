package com.ai.api.dto;

public class ErrorCode {
    /**
     * Group error code
     */
    public static final String GROUP_ERROR_NAME_EXIST = "ERROR-GROUP-000";
    public static final String GROUP_ERROR_NOT_FOUND = "ERROR-GROUP-001";

    /**
     * Permission error code
     */
    public static final String PERMISSION_ERROR_NAME_EXIST = "ERROR-PERMISSION-000";
    public static final String PERMISSION_ERROR_CODE_EXIST = "ERROR-PERMISSION-001";
    public static final String PERMISSION_ERROR_NOT_FOUND = "ERROR-PERMISSION-002";

    /**
     * Starting error code Account
     */
    public static final String ACCOUNT_ERROR_UNKNOWN = "ERROR-ACCOUNT-0000";
    public static final String ACCOUNT_ERROR_USERNAME_EXIST = "ERROR-ACCOUNT-0001";
    public static final String ACCOUNT_ERROR_NOT_FOUND = "ERROR-ACCOUNT-0002";
    public static final String ACCOUNT_ERROR_WRONG_PASSWORD = "ERROR-ACCOUNT-0003";
    public static final String ACCOUNT_ERROR_WRONG_HASH_RESET_PASS = "ERROR-ACCOUNT-0004";
    public static final String ACCOUNT_ERROR_LOCKED = "ERROR-ACCOUNT-0005";
    public static final String ACCOUNT_ERROR_OPT_INVALID = "ERROR-ACCOUNT-0006";
    public static final String ACCOUNT_ERROR_LOGIN = "ERROR-ACCOUNT-0007";
    public static final String ACCOUNT_ERROR_SOCIAL_LOGIN_FAIL = "ERROR-ACCOUNT-ERROR-0008";
    public static final String ACCOUNT_ERROR_NOT_DELETE_SUPPER_ADMIN = "ERROR-ACCOUNT-00014";
    public static final String ACCOUNT_ERROR_EMAIL_EXISTED = "ERROR-ACCOUNT-00015";
    public static final String ACCOUNT_ERROR_PHONE_EXISTED = "ERROR-ACCOUNT-00016";
    public static final String ACCOUNT_ERROR_NEW_PASSWORD_SAME_OLD_PASSWORD = "ERROR-ACCOUNT-00017";

    /**
     * GroupPermission error code
     */
    public static final String GROUP_PERMISSION_ERROR_NOT_FOUND = "ERROR-GROUP-PERMISSION-000";
    public static final String GROUP_PERMISSION_ERROR_NAME_EXIST = "ERROR-GROUP-PERMISSION-001";

    /**
     * Setting error code
     */
    public static final String SETTING_ERROR_NOT_FOUND = "ERROR-SETTING-000";
    public static final String SETTING_ERROR_EXISTED_GROUP_NAME_AND_KEY_NAME = "ERROR-SETTING-001";

    /**
     * Company error code
     */
    public static final String COMPANY_ERROR_NAME_EXIST = "ERROR-COMPANY-000";
    public static final String COMPANY_ERROR_NOT_FOUND = "ERROR-COMPANY-001";

    /**
     * Course error code
     */
    public static final String COURSE_ERROR_NAME_EXIST = "ERROR-COURSE-000";
    public static final String COURSE_ERROR_NOT_FOUND = "ERROR-COURSE-001";

    /**
     * Student error code
     */
    public static final String STUDENT_ERROR_GROUP_KIND_INVALID = "ERROR-STUDENT-000";
    public static final String STUDENT_ERROR_NOT_FOUND = "ERROR-STUDENT-001";

    /**
     * Mentor error code
     */
    public static final String MENTOR_ERROR_GROUP_KIND_INVALID = "ERROR-MENTOR-000";
    public static final String MENTOR_ERROR_NOT_FOUND = "ERROR-MENTOR-001";

    /**
     * Syllabus error code
     */
    public static final String SYLLABUS_ERROR_NOT_FOUND = "ERROR-SYLLABUS-000";
    public static final String SYLLABUS_ERROR_UNABLE_DELETE = "ERROR-SYLLABUS-001";

    /**
     * Rating error code
     */
    public static final String RATING_ERROR_NOT_FOUND = "ERROR-RATING-000";

    /**
     * Classroom error code
     */
    public static final String CLASSROOM_ERROR_NOT_FOUND = "ERROR-CLASSROOM-000";
    public static final String CLASSROOM_ERROR_UNABLE_DELETE = "ERROR-CLASSROOM-001";
    public static final String CLASSROOM_ERROR_INVALID_STATE_TRANSITION = "ERROR-CLASSROOM-002";
    public static final String CLASSROOM_ERROR_UNABLE_UPDATE = "ERROR-CLASSROOM-003";

    /**
     * Classroom-Student error code
     */
    public static final String CLASSROOM_STUDENT_ERROR_NOT_FOUND = "ERROR-CLASSROOM-STUDENT-000";
    public static final String CLASSROOM_STUDENT_ERROR_ALREADY_REGISTERED = "ERROR-CLASSROOM-STUDENT-001";
    public static final String CLASSROOM_STUDENT_ERROR_CLASSROOM_NOT_JOINABLE = "ERROR-CLASSROOM-STUDENT-002";
    public static final String CLASSROOM_STUDENT_ERROR_INVALID_STATE_TRANSITION = "ERROR-CLASSROOM-STUDENT-003";
    public static final String CLASSROOM_STUDENT_ERROR_UNABLE_DELETE = "ERROR-CLASSROOM-STUDENT-004";

    /**
     * Registration error code
     */
    public static final String REGISTRATION_ERROR_NOT_FOUND = "ERROR-REGISTRATION-000";
    public static final String REGISTRATION_ERROR_CLASSROOM_NOT_ACTIVE = "ERROR-REGISTRATION-001";
    public static final String REGISTRATION_ERROR_EMAIL_EXIST = "ERROR-REGISTRATION-002";
    public static final String REGISTRATION_ERROR_PHONE_EXIST = "ERROR-REGISTRATION-003";

    /**
     * Category error code
     */
    public static final String CATEGORY_ERROR_NAME_EXIST = "ERROR-CATEGORY-000";
    public static final String CATEGORY_ERROR_NOT_FOUND = "ERROR-CATEGORY-001";
    public static final String CATEGORY_ERROR_PARENT_NOT_FOUND = "ERROR-CATEGORY-002";
    public static final String CATEGORY_ERROR_INVALID_PARENT = "ERROR-CATEGORY-003";
    public static final String CATEGORY_ERROR_HAS_CHILDREN = "ERROR-CATEGORY-004";
    public static final String CATEGORY_ERROR_PARENT_NOT_ROOT = "ERROR-CATEGORY-005";

    /**
     * Tag error code
     */
    public static final String TAG_ERROR_NOT_FOUND = "ERROR-TAG-000";
    public static final String TAG_ERROR_IS_EXISTED = "ERROR-TAG-001";

    /**
     * Voucher error code
     */
    public static final String VOUCHER_ERROR_NOT_FOUND = "ERROR-VOUCHER-000";
    public static final String VOUCHER_ERROR_CODE_EXISTED = "ERROR-VOUCHER-001";
    public static final String VOUCHER_ERROR_ALREADY_DONE = "ERROR-VOUCHER-002";
    public static final String VOUCHER_ERROR_INVALID = "ERROR-VOUCHER-003";

    /**
     * News error code
     */
    public static final String NEWS_ERROR_NOT_FOUND = "ERROR-NEWS-000";
    public static final String NEWS_ERROR_CATEGORY_NOT_FOUND = "ERROR-NEWS-001";

    /**
     * Assignment error code
     */
    public static final String ASSIGNMENT_ERROR_NOT_FOUND = "ERROR-ASSIGNMENT-000";
    public static final String ASSIGNMENT_ERROR_UNABLE_UPDATE = "ERROR-ASSIGNMENT-001";

    /**
     * Submission error code
     */
    public static final String SUBMISSION_ERROR_NOT_FOUND = "ERROR-SUBMISSION-000";
    public static final String SUBMISSION_ERROR_ASSIGNMENT_CLOSED = "ERROR-SUBMISSION-001";
    public static final String SUBMISSION_ERROR_ALREADY_GRADED = "ERROR-SUBMISSION-002";
}
