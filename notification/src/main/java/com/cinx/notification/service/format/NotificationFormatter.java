package com.cinx.notification.service.format;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class NotificationFormatter {

    public NotificationMessage coursePublishedForInstructor(String courseId, String courseTitle, String instructorName) {
        String title = "Khóa học đã được xuất bản";
        String safeCourseTitle = defaultText(courseTitle, "khóa học của bạn");
        String name = defaultText(instructorName, "giảng viên");
        return message(
                "COURSE_PUBLISHED",
                title,
                "Khóa học \"" + safeCourseTitle + "\" đã được duyệt và xuất bản.",
                title + " - " + safeCourseTitle,
                paragraphs(
                        "Xin chào " + name + ",",
                        "Khóa học \"" + safeCourseTitle + "\" đã được quản trị viên duyệt và hiện đã được xuất bản.",
                        "Người học đã có thể truy cập nội dung mới nhất của khóa học."
                ),
                courseId,
                courseAction(courseId),
                metadata("courseId", courseId, "courseTitle", safeCourseTitle)
        );
    }

    public NotificationMessage coursePublishedForLearners(String courseId, String courseTitle) {
        String safeCourseTitle = defaultText(courseTitle, "khóa học bạn đang theo dõi");
        return message(
                "COURSE_CONTENT_PUBLISHED",
                "Nội dung khóa học mới",
                "Khóa học \"" + safeCourseTitle + "\" vừa có nội dung mới. Vào học ngay nhé.",
                "Nội dung mới - " + safeCourseTitle,
                paragraphs(
                        "Khóa học \"" + safeCourseTitle + "\" vừa có nội dung mới.",
                        "Hãy quay lại CINX để tiếp tục lộ trình học của bạn."
                ),
                courseId,
                courseAction(courseId),
                metadata("courseId", courseId, "courseTitle", safeCourseTitle)
        );
    }

    public NotificationMessage courseApprovalRequested(String courseId, String courseTitle, String instructorName) {
        String safeCourseTitle = defaultText(courseTitle, "một khóa học");
        String safeInstructorName = defaultText(instructorName, "Một giảng viên");
        return message(
                "COURSE_APPROVAL_REQUESTED",
                "Khóa học chờ duyệt",
                safeInstructorName + " đã gửi khóa học \"" + safeCourseTitle + "\" để chờ duyệt.",
                "Khóa học chờ duyệt - " + safeCourseTitle,
                paragraphs(safeInstructorName + " đã gửi khóa học \"" + safeCourseTitle + "\" để chờ duyệt."),
                courseId,
                "/admin/courses/" + courseId,
                metadata("courseId", courseId, "courseTitle", safeCourseTitle, "instructorName", safeInstructorName)
        );
    }

    public NotificationMessage certificateRequested(String requestId, String studentName, String courseId, String courseTitle) {
        String safeCourseTitle = defaultText(courseTitle, "khóa học của bạn");
        String safeStudentName = defaultText(studentName, "Một học viên");
        return message(
                "CERTIFICATE_REQUESTED",
                "Yêu cầu cấp chứng chỉ mới",
                safeStudentName + " đã yêu cầu cấp chứng chỉ cho khóa học \"" + safeCourseTitle + "\".",
                "Yêu cầu cấp chứng chỉ - " + safeCourseTitle,
                paragraphs(
                        safeStudentName + " đã yêu cầu cấp chứng chỉ cho khóa học \"" + safeCourseTitle + "\".",
                        "Vui lòng kiểm tra và xử lý yêu cầu trong trang quản lý."
                ),
                requestId,
                "/instructor/certificates/requests/" + requestId,
                metadata("requestId", requestId, "courseId", courseId, "courseTitle", safeCourseTitle)
        );
    }

    public NotificationMessage certificateApproved(String requestId, String userName, String courseId,
                                                   String courseTitle, String certificateUrl) {
        String safeCourseTitle = defaultText(courseTitle, "khóa học của bạn");
        String greeting = userName == null || userName.isBlank() ? "Chúc mừng!" : "Chúc mừng " + userName + "!";
        return message(
                "CERTIFICATE_APPROVED",
                "Chứng chỉ đã được duyệt",
                "Chứng chỉ của bạn cho khóa học \"" + safeCourseTitle + "\" đã được duyệt.",
                "Chứng chỉ đã được duyệt - " + safeCourseTitle,
                paragraphs(
                        greeting,
                        "Chứng chỉ của bạn cho khóa học \"" + safeCourseTitle + "\" đã được duyệt.",
                        certificateUrl == null || certificateUrl.isBlank()
                                ? "Bạn có thể xem chứng chỉ trong hồ sơ học tập."
                                : "<a href=\"" + escape(certificateUrl) + "\">Xem chứng chỉ</a>"
                ),
                requestId,
                certificateUrl,
                metadata("requestId", requestId, "courseId", courseId, "courseTitle", safeCourseTitle)
        );
    }

    public NotificationMessage courseCompleted(String courseId, String courseTitle) {
        String safeCourseTitle = defaultText(courseTitle, "khóa học");
        return message(
                "COURSE_COMPLETED",
                "Bạn đã hoàn thành khóa học",
                "Chúc mừng bạn đã hoàn thành khóa học \"" + safeCourseTitle + "\".",
                "Hoàn thành khóa học - " + safeCourseTitle,
                paragraphs(
                        "Chúc mừng bạn đã hoàn thành khóa học \"" + safeCourseTitle + "\".",
                        "Hãy tiếp tục duy trì nhịp học và khám phá các khóa học tiếp theo."
                ),
                courseId,
                courseAction(courseId),
                metadata("courseId", courseId, "courseTitle", safeCourseTitle)
        );
    }

    public NotificationMessage dailyReminder(String goalType, int targetValue, int currentValue, String targetItemId) {
        String message = switch (goalType == null ? "" : goalType) {
            case "XP" -> "Mục tiêu hôm nay của bạn là " + targetValue + " XP. Tiếp tục học nhé.";
            case "LEARNING_ITEMS_COMPLETED" -> "Bạn cần hoàn thành " + targetValue + " nội dung học trong hôm nay.";
            case "VIDEOS_COMPLETED" -> "Bạn cần hoàn thành " + targetValue + " bài học video trong hôm nay.";
            case "QUIZZES_PASSED" -> "Bạn cần vượt qua " + targetValue + " bài quiz trong hôm nay.";
            case "ASSIGNMENTS_SUBMITTED" -> "Bạn cần nộp " + targetValue + " bài tập trong hôm nay.";
            case "SPECIFIC_LESSON_COMPLETED" -> "Bạn có một bài học cần hoàn thành trong hôm nay.";
            default -> "Bạn có một mục tiêu học tập cần hoàn thành trong hôm nay.";
        };
        return message(
                "DAILY_LEARNING_REMINDER",
                "Đừng để mất nhịp học",
                message,
                "Nhắc học hôm nay",
                paragraphs(message, "Mở CINX để tiếp tục tiến độ học tập của bạn."),
                targetItemId,
                targetItemId == null ? "/learning" : "/learning/items/" + targetItemId,
                metadata("goalType", goalType, "targetValue", targetValue, "currentValue", currentValue, "targetItemId", targetItemId)
        );
    }

    public NotificationMessage paymentSucceeded(String orderId, String formattedPrice, String userName) {
        String price = defaultText(formattedPrice, "0 VND");
        return message(
                "PAYMENT_SUCCEEDED",
                "Thanh toán thành công",
                "Thanh toán cho đơn hàng " + orderId + " (" + price + ") đã được xử lý thành công.",
                "Xác nhận thanh toán - Đơn hàng " + orderId,
                paragraphs(
                        "Xin chào " + defaultText(userName, "bạn") + ",",
                        "CINX đã nhận thanh toán cho đơn hàng \"" + orderId + "\".",
                        "Tổng tiền: " + price + ". Cảm ơn bạn đã mua khóa học."
                ),
                orderId,
                "/orders/" + orderId,
                metadata("orderId", orderId, "totalPrice", price)
        );
    }

    public NotificationMessage orderCreated(String orderId) {
        return message(
                "ORDER_CREATED",
                "Đơn hàng đã được tạo",
                "Đơn hàng " + orderId + " đã được tạo thành công.",
                "Đơn hàng đã được tạo - " + orderId,
                paragraphs("Đơn hàng " + orderId + " đã được tạo thành công."),
                orderId,
                "/orders/" + orderId,
                metadata("orderId", orderId)
        );
    }

    public NotificationMessage orderCancelled(String orderId, String formattedPrice, String userName) {
        String price = defaultText(formattedPrice, "0 VND");
        return message(
                "ORDER_CANCELLED",
                "Đơn hàng đã bị hủy",
                "Đơn hàng " + orderId + " (" + price + ") đã bị hủy.",
                "Đơn hàng đã bị hủy - " + orderId,
                paragraphs(
                        "Xin chào " + defaultText(userName, "bạn") + ",",
                        "Đơn hàng \"" + orderId + "\" đã bị hủy.",
                        "Tổng tiền: " + price + "."
                ),
                orderId,
                "/orders/" + orderId,
                metadata("orderId", orderId, "totalPrice", price)
        );
    }

    public NotificationMessage courseReviewCreated(String courseId, String courseTitle, String reviewerName, Double rating) {
        String safeCourseTitle = defaultText(courseTitle, "khóa học của bạn");
        String ratingText = rating == null ? "một đánh giá mới" : String.format("%.1f sao", rating);
        return message(
                "COURSE_REVIEW_CREATED",
                "Đánh giá mới cho khóa học",
                defaultText(reviewerName, "Một học viên") + " đã để lại đánh giá " + ratingText
                        + " cho khóa học \"" + safeCourseTitle + "\".",
                "Đánh giá mới - " + safeCourseTitle,
                paragraphs(defaultText(reviewerName, "Một học viên") + " đã để lại đánh giá " + ratingText
                        + " cho khóa học \"" + safeCourseTitle + "\"."),
                courseId,
                courseAction(courseId),
                metadata("courseId", courseId, "courseTitle", safeCourseTitle, "rating", rating)
        );
    }

    public NotificationMessage courseQuestionCreated(String courseId, String courseTitle, String questionId, String questionTitle) {
        String safeCourseTitle = defaultText(courseTitle, "khóa học của bạn");
        String safeQuestionTitle = defaultText(questionTitle, "một câu hỏi mới");
        return message(
                "COURSE_QUESTION_CREATED",
                "Câu hỏi mới trong khóa học",
                "Có câu hỏi mới trong \"" + safeCourseTitle + "\": " + safeQuestionTitle,
                "Câu hỏi mới - " + safeCourseTitle,
                paragraphs("Có câu hỏi mới trong \"" + safeCourseTitle + "\": " + safeQuestionTitle),
                questionId,
                courseAction(courseId) + "/questions/" + questionId,
                metadata("courseId", courseId, "courseTitle", safeCourseTitle, "questionId", questionId)
        );
    }

    public NotificationMessage courseAnswerCreated(String courseId, String questionId, String answerId, boolean replyToAnswer) {
        return message(
                "COURSE_ANSWER_CREATED",
                "Phản hồi mới trong Q&A",
                replyToAnswer
                        ? "Có người đã phản hồi câu trả lời của bạn trong Q&A khóa học."
                        : "Có người đã trả lời câu hỏi của bạn trong Q&A khóa học.",
                "Phản hồi mới trong Q&A",
                paragraphs(replyToAnswer
                        ? "Có người đã phản hồi câu trả lời của bạn trong Q&A khóa học."
                        : "Có người đã trả lời câu hỏi của bạn trong Q&A khóa học."),
                answerId,
                courseAction(courseId) + "/questions/" + questionId,
                metadata("courseId", courseId, "questionId", questionId, "answerId", answerId)
        );
    }

    private NotificationMessage message(String type, String title, String message, String subject, String htmlBody,
                                        String referenceId, String actionUrl, Map<String, Object> metadata) {
        return new NotificationMessage(type, title, message, subject, htmlBody, referenceId, actionUrl, metadata);
    }

    private String paragraphs(String... lines) {
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            if (line.startsWith("<a ")) {
                builder.append("<p>").append(line).append("</p>");
            } else {
                builder.append("<p>").append(escape(line)).append("</p>");
            }
        }
        return builder.toString();
    }

    private Map<String, Object> metadata(Object... keyValues) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            if (keyValues[i] != null && keyValues[i + 1] != null) {
                metadata.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
            }
        }
        return metadata;
    }

    private String courseAction(String courseId) {
        return courseId == null || courseId.isBlank() ? "/courses" : "/courses/" + courseId;
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String escape(String value) {
        return HtmlUtils.htmlEscape(value);
    }
}
