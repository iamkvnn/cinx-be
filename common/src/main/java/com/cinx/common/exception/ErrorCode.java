package com.cinx.common.exception;

public enum ErrorCode {
    RESOURCE_NOT_FOUND("Resource not found"),
    BAD_REQUEST("Bad request"),
    VALIDATION_FAILED("Validation failed"),
    UNAUTHORIZED("Unauthorized"),
    FORBIDDEN("Forbidden"),
    RESOURCE_ALREADY_EXISTS("Resource already exists"),
    INTERNAL_ERROR("Internal server error"),

    INVALID_CREDENTIALS("Invalid credentials"),
    GOOGLE_ACCOUNT_LOGIN_REQUIRED("Google account login required"),
    EMAIL_NOT_VERIFIED("Email not verified"),
    USER_ACCOUNT_BANNED("User account banned"),
    INSTRUCTOR_NOT_VERIFIED("Instructor not verified"),
    REFRESH_TOKEN_INVALID("Invalid refresh token"),
    INVALID_OTP("Invalid OTP"),
    OTP_EXPIRED("OTP expired"),
    INVALID_OLD_PASSWORD("Invalid old password"),
    BAN_DURATION_REQUIRED("Ban duration required"),
    BAN_DURATION_EXCEEDED("Ban duration exceeded"),

    DATE_RANGE_INVALID("Invalid date range"),
    STATISTICS_RANGE_TOO_LARGE("Statistics range too large"),
    INVALID_PAGINATION("Invalid pagination"),
    INVALID_SORT("Invalid sort"),

    COURSE_ARCHIVED("Course archived"),
    COURSE_WAITING_APPROVAL("Course waiting for approval"),
    COURSE_STATUS_INVALID("Invalid course status"),
    COURSE_DRAFT_MISSING("Course draft missing"),
    COURSE_UNAVAILABLE_FOR_PURCHASE("Course unavailable for purchase"),
    NOT_ENROLLED_IN_COURSE("Not enrolled in course"),
    INSTRUCTOR_ACCESS_REQUIRED("Instructor access required"),

    ORDER_ITEMS_REQUIRED("Order items required"),
    VOUCHER_INVALID("Invalid voucher"),
    VOUCHER_EXPIRED("Voucher expired"),
    VOUCHER_NOT_ACTIVE("Voucher not active"),
    VOUCHER_MIN_PURCHASE_NOT_MET("Voucher minimum purchase not met"),
    VOUCHER_OUT_OF_STOCK("Voucher out of stock"),
    PAYMENT_ALREADY_PAID("Payment already paid"),

    CART_ITEM_ALREADY_EXISTS("Cart item already exists"),
    ALREADY_UPVOTED("Already upvoted"),
    ASSIGNMENT_ALREADY_SUBMITTED("Assignment already submitted"),
    CERTIFICATE_ALREADY_APPLIED("Certificate already applied"),
    CERTIFICATE_NOT_AVAILABLE("Certificate not available"),
    COURSE_NOT_COMPLETED("Course not completed"),
    CERTIFICATE_REQUEST_NOT_PENDING("Certificate request not pending"),
    LEARNING_PATH_ALREADY_ACTIVE("Learning path already active"),

    QUIZ_REVIEW_NOT_ALLOWED("Quiz review not allowed"),
    QUIZ_SESSION_ALREADY_IN_PROGRESS("Quiz session already in progress"),
    QUIZ_ATTEMPT_LIMIT_REACHED("Quiz attempt limit reached"),
    QUIZ_SESSION_NOT_IN_PROGRESS("Quiz session not in progress"),
    QUIZ_SESSION_EXPIRED("Quiz session expired"),
    QUIZ_SESSION_NOT_PENDING_GRADING("Quiz session not pending grading"),
    QUIZ_ANSWER_INVALID("Invalid quiz answer"),
    QUIZ_ESSAY_SCORE_INVALID("Invalid quiz essay score"),
    QUIZ_CONFIGURATION_INVALID("Invalid quiz configuration"),

    LESSON_ORDER_INVALID("Invalid lesson order"),
    LESSON_PREREQUISITE_INVALID("Invalid lesson prerequisite"),
    LESSON_PREREQUISITE_CYCLE("Lesson prerequisite cycle"),
    LESSON_TYPE_INVALID("Invalid lesson type"),
    VIDEO_POSITION_INVALID("Invalid video position"),
    VIDEO_QUESTION_ANSWER_INCORRECT("Incorrect video question answer"),
    VIDEO_QUESTION_TIMESTAMP_INVALID("Invalid video question timestamp"),

    SUBTITLE_INVALID("Invalid subtitle"),
    SUBTITLE_FILE_UNSUPPORTED("Unsupported subtitle file"),
    SUBTITLE_FILE_INVALID("Invalid subtitle file"),
    SUBTITLE_FILE_TOO_LARGE("Subtitle file too large"),

    NOT_RESOURCE_OWNER("Not resource owner");

    private final String title;

    ErrorCode(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
