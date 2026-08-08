package com.ai.api.constant;

public class AIConstant {
    public static final String DEFAULT_TIMEZONE = "UTC";

    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String HEADER_CLIENT_TYPE = "X-Client-Type";
    public static final String HEADER_CLIENT_TYPE_WEB = "WEB";

    public static final String PHONE_PATTERN = "^0[35789][0-9]{8}$";
    public static final String EMAIL_PATTERN = "^(?!.*[.]{2,})[a-zA-Z0-9.%]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String USERNAME_PATTERN = "^(?=.{3,20}$)(?!.*[_.]{2})[a-zA-Z][a-zA-Z0-9_]*[a-zA-Z0-9]$";
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";

    public static final Integer USER_KIND_ADMIN = 1;
    public static final Integer USER_KIND_MENTOR = 2;
    public static final Integer USER_KIND_STUDENT = 3;

    public static final Integer STATUS_ACTIVE = 1;
    public static final Integer STATUS_PENDING = 0;
    public static final Integer STATUS_LOCK = -1;
    public static final Integer STATUS_DELETE = -2;

    public static final Integer GROUP_KIND_ADMIN = 1;
    public static final Integer GROUP_KIND_MENTOR = 2;
    public static final Integer GROUP_KIND_STUDENT = 3;

    public static final Integer SYLLABUS_KIND_CHAPTER = 1;
    public static final Integer SYLLABUS_KIND_LESSON = 2;

    public static final Integer RATING_STAR_MIN = 1;
    public static final Integer RATING_STAR_MAX = 5;

    public static final Integer CLASSROOM_STATE_PENDING = 0;
    public static final Integer CLASSROOM_STATE_ACTIVE = 1;
    public static final Integer CLASSROOM_STATE_DONE = 2;
    public static final Integer CLASSROOM_STATE_CANCEL = 3;

    public static final Integer CLASSROOM_STUDENT_STATE_PENDING = 0;
    public static final Integer CLASSROOM_STUDENT_STATE_ACCEPT = 1;
    public static final Integer CLASSROOM_STUDENT_STATE_REJECT = 2;

    public static final int TAG_NAME_MAX_LENGTH = 100;
    public static final int TAG_COLOR_CODE_MAX_LENGTH = 7;

    public static final Integer VOUCHER_TYPE_PERCENT = 1;
    public static final Integer VOUCHER_TYPE_FIXED_AMOUNT = 2;

    public static final Integer VOUCHER_STATE_PENDING = 0;
    public static final Integer VOUCHER_STATE_ACTIVE = 1;
    public static final Integer VOUCHER_STATE_DONE = 2;

    public static final Integer ASSIGNMENT_STATE_DRAFT = 0;
    public static final Integer ASSIGNMENT_STATE_PUBLISHED = 1;
    public static final Integer ASSIGNMENT_STATE_CLOSED = 2;

    public static final Integer SUBMISSION_STATE_PENDING = 0;
    public static final Integer SUBMISSION_STATE_GRADED = 1;

    private AIConstant() {
        throw new IllegalStateException("Utility class");
    }
}
