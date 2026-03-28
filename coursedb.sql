/*
 Navicat Premium Dump SQL

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 90001 (9.0.1)
 Source Host           : localhost:3306
 Source Schema         : coursedb

 Target Server Type    : MySQL
 Target Server Version : 90001 (9.0.1)
 File Encoding         : 65001

 Date: 26/03/2026 13:39:31
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for article_lesson
-- ----------------------------
DROP TABLE IF EXISTS `article_lesson`;
CREATE TABLE `article_lesson`  (
  `lesson_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`lesson_id`) USING BTREE,
  CONSTRAINT `FKoi0bna8ekqysw6xpxll4n86jg` FOREIGN KEY (`lesson_id`) REFERENCES `lesson` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of article_lesson
-- ----------------------------

-- ----------------------------
-- Table structure for assignment_attachment
-- ----------------------------
DROP TABLE IF EXISTS `assignment_attachment`;
CREATE TABLE `assignment_attachment`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `version` bigint NOT NULL,
  `attachment_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_size` bigint NULL DEFAULT NULL,
  `file_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assignment_attachment
-- ----------------------------

-- ----------------------------
-- Table structure for assignment_lesson
-- ----------------------------
DROP TABLE IF EXISTS `assignment_lesson`;
CREATE TABLE `assignment_lesson`  (
  `lesson_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `end_time` datetime(6) NULL DEFAULT NULL,
  `start_time` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`lesson_id`) USING BTREE,
  CONSTRAINT `FKy8yaiire10ly6dpnes76bpuo` FOREIGN KEY (`lesson_id`) REFERENCES `lesson` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assignment_lesson
-- ----------------------------

-- ----------------------------
-- Table structure for assignment_lesson_attachments
-- ----------------------------
DROP TABLE IF EXISTS `assignment_lesson_attachments`;
CREATE TABLE `assignment_lesson_attachments`  (
  `assignment_lesson_lesson_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `attachments_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  UNIQUE INDEX `UKlu5dbre94fdlrj1q0n2e5lymj`(`attachments_id` ASC) USING BTREE,
  INDEX `FK3t63yfxykn3ipvdu8m65vkf3k`(`assignment_lesson_lesson_id` ASC) USING BTREE,
  CONSTRAINT `FK3t63yfxykn3ipvdu8m65vkf3k` FOREIGN KEY (`assignment_lesson_lesson_id`) REFERENCES `assignment_lesson` (`lesson_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK61vvro024vmrmsg8w4o8byir4` FOREIGN KEY (`attachments_id`) REFERENCES `assignment_attachment` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assignment_lesson_attachments
-- ----------------------------

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `version` bigint NOT NULL,
  `created_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `updated_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES ('26b384ed-6aa2-47fa-9f71-7d994627d7f1', 'Marketing', 0, NULL, NULL, NULL, NULL);
INSERT INTO `category` VALUES ('470199fa-9067-423d-b0a5-a77980484125', 'AI', 0, NULL, NULL, NULL, NULL);
INSERT INTO `category` VALUES ('752ee7d2-3c3b-4f19-8c1a-3b7501191a47', 'Cloud Computing', 0, NULL, NULL, NULL, NULL);
INSERT INTO `category` VALUES ('d3b7af49-1862-479b-9b10-aa18d2fd7bd1', 'Design', 0, NULL, NULL, NULL, NULL);
INSERT INTO `category` VALUES ('d63acb8f-7d9b-4cc3-af1e-68c63fbc9468', 'Data Engineering', 0, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for category_courses
-- ----------------------------
DROP TABLE IF EXISTS `category_courses`;
CREATE TABLE `category_courses`  (
  `category_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `courses_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`category_id`, `courses_id`) USING BTREE,
  UNIQUE INDEX `UKapfid292cw6fskdfwbxxg7off`(`courses_id` ASC) USING BTREE,
  CONSTRAINT `FKh0p3gweu0fdpc0urjuf3nnhu2` FOREIGN KEY (`courses_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKlklg46yg9n2dqicxahmvj8bye` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category_courses
-- ----------------------------
INSERT INTO `category_courses` VALUES ('470199fa-9067-423d-b0a5-a77980484125', 'e19bbf50-7daa-4444-9fba-fceb99af2dff');

-- ----------------------------
-- Table structure for course
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `duration` bigint NULL DEFAULT NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `price` bigint NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `category_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `instructor_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `discounted_price` bigint NULL DEFAULT NULL,
  `enrollment_count` bigint NULL DEFAULT NULL,
  `is_in_subscription` bit(1) NULL DEFAULT NULL,
  `is_published` bit(1) NULL DEFAULT NULL,
  `rating` double NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `discount_rate` bigint NULL DEFAULT NULL,
  `version` bigint NOT NULL,
  `created_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `updated_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKkyes7515s3ypoovxrput029bh`(`category_id` ASC) USING BTREE,
  INDEX `FKqk2yq2yk124dhlsilomy36qr9`(`instructor_id` ASC) USING BTREE,
  CONSTRAINT `FKkyes7515s3ypoovxrput029bh` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKqk2yq2yk124dhlsilomy36qr9` FOREIGN KEY (`instructor_id`) REFERENCES `instructor` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course
-- ----------------------------
INSERT INTO `course` VALUES ('23ee09b1-71c4-499b-9fde-7e261f74e294', 'abc', 110, NULL, 888888, 'AI', '470199fa-9067-423d-b0a5-a77980484125', NULL, 877777, 150, b'0', b'1', 5, '2026-02-05 00:22:26.000000', '2026-02-05 00:22:29.000000', 1, 0, NULL, NULL);
INSERT INTO `course` VALUES ('3b051544-024a-11f1-a90d-005056c00001', 'Học AI từ cơ bản đến nâng cao', 120, NULL, 1200000, 'AI Fundamentals', '470199fa-9067-423d-b0a5-a77980484125', '439dfea8-1807-4c0b-9e9d-d0379c8e761d', 990000, 320, b'1', b'1', 4.8, '2026-02-05 11:22:06.000000', '2026-02-05 11:22:06.000000', 18, 0, NULL, NULL);
INSERT INTO `course` VALUES ('3b07b222-024a-11f1-a90d-005056c00001', 'Ứng dụng Machine Learning thực tế', 150, NULL, 1500000, 'Machine Learning Bootcamp', '470199fa-9067-423d-b0a5-a77980484125', '439dfea8-1807-4c0b-9e9d-d0379c8e761d', 1290000, 280, b'0', b'1', 4.7, '2026-02-05 11:22:06.000000', '2026-02-05 11:22:06.000000', 14, 0, NULL, NULL);
INSERT INTO `course` VALUES ('3b07bffd-024a-11f1-a90d-005056c00001', 'Xây dựng hệ thống xử lý dữ liệu lớn', 180, NULL, 1700000, 'Data Engineering with Spark', 'd63acb8f-7d9b-4cc3-af1e-68c63fbc9468', '439dfea8-1807-4c0b-9e9d-d0379c8e761d', 1450000, 210, b'1', b'1', 4.6, '2026-02-05 11:22:06.000000', '2026-02-05 11:22:06.000000', 15, 0, NULL, NULL);
INSERT INTO `course` VALUES ('3b07c2b5-024a-11f1-a90d-005056c00001', 'Data Pipeline & ETL chuyên sâu', 140, NULL, 1400000, 'ETL & Data Pipeline', 'd63acb8f-7d9b-4cc3-af1e-68c63fbc9468', '439dfea8-1807-4c0b-9e9d-d0379c8e11dd', 1190000, 190, b'0', b'1', 4.5, '2026-02-05 11:22:06.000000', '2026-02-05 11:22:06.000000', 15, 0, NULL, NULL);
INSERT INTO `course` VALUES ('3b07c41a-024a-11f1-a90d-005056c00001', 'Triển khai hệ thống với AWS', 160, NULL, 1600000, 'AWS Cloud Practitioner', '752ee7d2-3c3b-4f19-8c1a-3b7501191a47', '439dfea8-1807-4c0b-9e9d-d0379c8e11dd', 1390000, 260, b'1', b'1', 4.7, '2026-02-05 11:22:06.000000', '2026-02-05 11:22:06.000000', 13, 0, NULL, NULL);
INSERT INTO `course` VALUES ('3b07c50e-024a-11f1-a90d-005056c00001', 'Kubernetes cho backend developer', 130, NULL, 1350000, 'Kubernetes in Practice', '752ee7d2-3c3b-4f19-8c1a-3b7501191a47', '439dfea8-1807-4c0b-9e9d-d0379c8e11dd', 1150000, 230, b'0', b'1', 4.6, '2026-02-05 11:22:06.000000', '2026-02-05 11:22:06.000000', 15, 0, NULL, NULL);
INSERT INTO `course` VALUES ('3b07c6e4-024a-11f1-a90d-005056c00001', 'Thiết kế UI/UX hiện đại', 100, NULL, 900000, 'UI/UX Design Basics', 'd3b7af49-1862-479b-9b10-aa18d2fd7bd1', '439dfea8-1807-4c0b-9e9d-d0379c8e764d', 790000, 410, b'1', b'1', 4.9, '2026-02-05 11:22:06.000000', '2026-02-05 11:22:06.000000', 12, 0, NULL, NULL);
INSERT INTO `course` VALUES ('3b07c8c8-024a-11f1-a90d-005056c00001', 'Figma từ cơ bản đến nâng cao', 90, NULL, 850000, 'Mastering Figma', 'd3b7af49-1862-479b-9b10-aa18d2fd7bd1', '439dfea8-1807-4c0b-9e9d-d0379c8e7644439dfea8-1807-4c0b-9e9d-d0379c8e7644', 720000, 380, b'1', b'1', 4.8, '2026-02-05 11:22:06.000000', '2026-02-05 11:22:06.000000', 15, 0, NULL, NULL);
INSERT INTO `course` VALUES ('3b07ca85-024a-11f1-a90d-005056c00001', 'Chiến lược Digital Marketing toàn diện', 110, NULL, 1000000, 'Digital Marketing Strategy', '26b384ed-6aa2-47fa-9f71-7d994627d7f1', NULL, 850000, 500, b'0', b'1', 4.6, '2026-02-05 11:22:06.000000', '2026-02-05 11:22:06.000000', 15, 0, NULL, NULL);
INSERT INTO `course` VALUES ('3b07cb6b-024a-11f1-a90d-005056c00001', 'SEO & Content Marketing thực chiến', 95, NULL, 950000, 'SEO & Content Marketing', '26b384ed-6aa2-47fa-9f71-7d994627d7f1', NULL, 800000, 460, b'1', b'1', 4.7, '2026-02-05 11:22:06.000000', '2026-02-05 11:22:06.000000', 16, 0, NULL, NULL);
INSERT INTO `course` VALUES ('97c88c68-09e2-4ce4-a129-b3b12c534637', 'Mastering MLOps', 90, NULL, 850000, 'Mastering MLOps', '470199fa-9067-423d-b0a5-a77980484125', '439dfea8-1807-4c0b-9e9d-d0379c8e11dd', 720000, 0, b'0', b'1', 0, '2026-03-23 05:22:55.759775', '2026-03-23 05:22:55.759775', 15, 0, NULL, NULL);
INSERT INTO `course` VALUES ('e19bbf50-7daa-4444-9fba-fceb99af2dff', 'abc', 120, NULL, 999999, 'Machine Learning', '470199fa-9067-423d-b0a5-a77980484125', NULL, 999999, 120, b'0', b'1', 5, '2026-02-04 00:22:35.000000', '2026-02-05 00:22:42.000000', 0, 0, NULL, NULL);

-- ----------------------------
-- Table structure for course_image
-- ----------------------------
DROP TABLE IF EXISTS `course_image`;
CREATE TABLE `course_image`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `public_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `course_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `version` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK5uweik1v6wv796ggohfekp7wb`(`course_id` ASC) USING BTREE,
  CONSTRAINT `FK5uweik1v6wv796ggohfekp7wb` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_image
-- ----------------------------
INSERT INTO `course_image` VALUES ('2f17f9ea-b14c-41a7-9693-24f570f28b4a', '2026-03-23 14:34:37.997170', '2026-03-23 14:34:37.997170', 'http://res.cloudinary.com/dxjusps3n/image/upload/v1/course_images/3b07c8c8-024a-11f1-a90d-005056c00001/Mastering_Figma_2', 'course_images/3b07c8c8-024a-11f1-a90d-005056c00001/Mastering_Figma_2', '3b07c8c8-024a-11f1-a90d-005056c00001', 0);
INSERT INTO `course_image` VALUES ('5f875d04-4a00-46ca-9b7f-78cbd530d705', '2026-03-23 14:34:37.994759', '2026-03-23 14:34:37.994759', 'http://res.cloudinary.com/dxjusps3n/image/upload/v1/course_images/3b07c8c8-024a-11f1-a90d-005056c00001/Mastering_Figma_1', 'course_images/3b07c8c8-024a-11f1-a90d-005056c00001/Mastering_Figma_1', '3b07c8c8-024a-11f1-a90d-005056c00001', 0);
INSERT INTO `course_image` VALUES ('8e05172b-82a5-49da-ad91-831b7fbd9522', '2026-03-23 14:24:54.726538', '2026-03-23 14:24:54.726538', 'http://res.cloudinary.com/dxjusps3n/image/upload/v1/course_images/97c88c68-09e2-4ce4-a129-b3b12c534637/Mastering_MLOps_11', 'course_images/97c88c68-09e2-4ce4-a129-b3b12c534637/Mastering_MLOps_11', '97c88c68-09e2-4ce4-a129-b3b12c534637', 0);
INSERT INTO `course_image` VALUES ('a47ca792-aca2-4fc8-811e-b16c0318e180', '2026-03-23 14:31:18.698367', '2026-03-23 14:31:18.698367', 'http://res.cloudinary.com/dxjusps3n/image/upload/v1/course_images/97c88c68-09e2-4ce4-a129-b3b12c534637/Mastering_MLOps_3', 'course_images/97c88c68-09e2-4ce4-a129-b3b12c534637/Mastering_MLOps_3', '97c88c68-09e2-4ce4-a129-b3b12c534637', 0);
INSERT INTO `course_image` VALUES ('bd140a00-21d0-4ac1-b5bd-694934c12a81', '2026-03-23 14:31:18.708642', '2026-03-23 14:31:18.708642', 'http://res.cloudinary.com/dxjusps3n/image/upload/v1/course_images/97c88c68-09e2-4ce4-a129-b3b12c534637/Mastering_MLOps_4', 'course_images/97c88c68-09e2-4ce4-a129-b3b12c534637/Mastering_MLOps_4', '97c88c68-09e2-4ce4-a129-b3b12c534637', 0);
INSERT INTO `course_image` VALUES ('c0aa69da-9a63-4487-9b0d-2691354ff0cd', '2026-03-23 14:24:54.677575', '2026-03-23 14:24:54.677575', 'http://res.cloudinary.com/dxjusps3n/image/upload/v1/course_images/97c88c68-09e2-4ce4-a129-b3b12c534637/Mastering_MLOps_01', 'course_images/97c88c68-09e2-4ce4-a129-b3b12c534637/Mastering_MLOps_01', '97c88c68-09e2-4ce4-a129-b3b12c534637', 0);

-- ----------------------------
-- Table structure for course_sections
-- ----------------------------
DROP TABLE IF EXISTS `course_sections`;
CREATE TABLE `course_sections`  (
  `course_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sections_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`course_id`, `sections_id`) USING BTREE,
  UNIQUE INDEX `UK1gmn3behel84aeia5pxkenna2`(`sections_id` ASC) USING BTREE,
  CONSTRAINT `FK2xxmsm6jx70yficps6d025wop` FOREIGN KEY (`sections_id`) REFERENCES `section` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKeo0tkj2kbwj547kgjgl5fagei` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_sections
-- ----------------------------
INSERT INTO `course_sections` VALUES ('97c88c68-09e2-4ce4-a129-b3b12c534637', '76b692e4-4f8c-44f6-a129-9e1c25b1f282');
INSERT INTO `course_sections` VALUES ('97c88c68-09e2-4ce4-a129-b3b12c534637', '97dffacd-eef1-4245-aaaf-38ccb239d9da');
INSERT INTO `course_sections` VALUES ('97c88c68-09e2-4ce4-a129-b3b12c534637', 'cc9e6d3f-7ce6-4aa9-903e-62d13095642c');

-- ----------------------------
-- Table structure for course_tag
-- ----------------------------
DROP TABLE IF EXISTS `course_tag`;
CREATE TABLE `course_tag`  (
  `course_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `tag_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`course_id`, `tag_id`) USING BTREE,
  INDEX `FKj7piuv0dh0v01l3aolwwd1jwh`(`tag_id` ASC) USING BTREE,
  CONSTRAINT `FK3tta6lkm8fr0rgfyr4y3xrr3u` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKj7piuv0dh0v01l3aolwwd1jwh` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_tag
-- ----------------------------

-- ----------------------------
-- Table structure for instructor
-- ----------------------------
DROP TABLE IF EXISTS `instructor`;
CREATE TABLE `instructor`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `bio` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `profile_picture_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of instructor
-- ----------------------------
INSERT INTO `instructor` VALUES ('439dfea8-1807-4c0b-9e9d-d0379c8e11dd', NULL, 'Huỳnh Xuân Phụng', NULL, NULL);
INSERT INTO `instructor` VALUES ('439dfea8-1807-4c0b-9e9d-d0379c8e761d', NULL, 'Trần Quang Khải', NULL, NULL);
INSERT INTO `instructor` VALUES ('439dfea8-1807-4c0b-9e9d-d0379c8e7644439dfea8-1807-4c0b-9e9d-d0379c8e7644', NULL, 'Nguyễn Hữu Trung', NULL, NULL);
INSERT INTO `instructor` VALUES ('439dfea8-1807-4c0b-9e9d-d0379c8e764a', NULL, 'Nguyễn Trần Thi Văn', NULL, NULL);
INSERT INTO `instructor` VALUES ('439dfea8-1807-4c0b-9e9d-d0379c8e764d', NULL, 'Nguyễn Thủy An', NULL, NULL);

-- ----------------------------
-- Table structure for instructor_courses
-- ----------------------------
DROP TABLE IF EXISTS `instructor_courses`;
CREATE TABLE `instructor_courses`  (
  `instructor_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `courses_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`instructor_id`, `courses_id`) USING BTREE,
  UNIQUE INDEX `UKg5fcmku2fuo8esu83qrn3ud0j`(`courses_id` ASC) USING BTREE,
  CONSTRAINT `FK6le8nm8s2bbpxfuht9mkc2sx5` FOREIGN KEY (`courses_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK9bta2cjr5t7pgwp9h2ecb0x3q` FOREIGN KEY (`instructor_id`) REFERENCES `instructor` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of instructor_courses
-- ----------------------------

-- ----------------------------
-- Table structure for lecture
-- ----------------------------
DROP TABLE IF EXISTS `lecture`;
CREATE TABLE `lecture`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `duration` bigint NULL DEFAULT NULL,
  `order_index` int NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `section_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `lecture_type` tinyint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK568elaju5okd8k0hukt18mtk7`(`section_id` ASC) USING BTREE,
  CONSTRAINT `FK568elaju5okd8k0hukt18mtk7` FOREIGN KEY (`section_id`) REFERENCES `section` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `lecture_chk_1` CHECK (`lecture_type` between 0 and 3)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of lecture
-- ----------------------------
INSERT INTO `lecture` VALUES ('19c199ba-8934-4510-a861-ddf7da364aa9', NULL, 10, 3, 'Bai 3', '76b692e4-4f8c-44f6-a129-9e1c25b1f282', 1);
INSERT INTO `lecture` VALUES ('203f66e3-c03c-4bf7-b5f3-1fc627f8fcb0', NULL, 10, 3, 'Bai 3', '97dffacd-eef1-4245-aaaf-38ccb239d9da', 1);
INSERT INTO `lecture` VALUES ('44b3f429-5b90-40fe-8558-20a6ac35bc8f', NULL, 10, 2, 'Bai 2', '76b692e4-4f8c-44f6-a129-9e1c25b1f282', 2);
INSERT INTO `lecture` VALUES ('5a47e8da-fdad-49af-9469-5870aaadeba3', NULL, 10, 1, 'Bai 1', '76b692e4-4f8c-44f6-a129-9e1c25b1f282', 0);
INSERT INTO `lecture` VALUES ('7ad62a16-0e06-4863-ad27-1abdc332aef7', NULL, 10, 2, 'Bai 2', 'cc9e6d3f-7ce6-4aa9-903e-62d13095642c', 2);
INSERT INTO `lecture` VALUES ('7fd93065-d3a0-49ad-8e2c-a1a44572d792', NULL, 10, 3, 'Bai 3', 'cc9e6d3f-7ce6-4aa9-903e-62d13095642c', 1);
INSERT INTO `lecture` VALUES ('83405274-ba1d-4099-a11d-7baffb6701e4', NULL, 10, 1, 'Bai 1', 'cc9e6d3f-7ce6-4aa9-903e-62d13095642c', 0);
INSERT INTO `lecture` VALUES ('cbe798c9-6fab-455a-aa44-a393806b3473', NULL, 10, 2, 'Bai 2', '97dffacd-eef1-4245-aaaf-38ccb239d9da', 2);
INSERT INTO `lecture` VALUES ('fc44b3b2-abc7-4def-b26c-67a38701b7b0', NULL, 10, 1, 'Bai 1', '97dffacd-eef1-4245-aaaf-38ccb239d9da', 0);

-- ----------------------------
-- Table structure for lesson
-- ----------------------------
DROP TABLE IF EXISTS `lesson`;
CREATE TABLE `lesson`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `version` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `created_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `updated_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `duration` bigint NULL DEFAULT NULL,
  `lecture_type` tinyint NULL DEFAULT NULL,
  `order_index` int NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `article_lesson_lesson_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `assessment_lesson_lesson_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `quiz_lesson_lesson_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `section_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `video_lesson_lesson_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK98vfp3ygo2qfode491be588dh`(`article_lesson_lesson_id` ASC) USING BTREE,
  UNIQUE INDEX `UKgkb1113ndfjmo3w169wqmb1tu`(`assessment_lesson_lesson_id` ASC) USING BTREE,
  UNIQUE INDEX `UKbuoafdqaq63vr7k2skc3vcpjm`(`quiz_lesson_lesson_id` ASC) USING BTREE,
  UNIQUE INDEX `UKmm4m4ca20ci6t5fkd2525lraj`(`video_lesson_lesson_id` ASC) USING BTREE,
  INDEX `FKl7uog042qrnneaje24n3vc10x`(`section_id` ASC) USING BTREE,
  CONSTRAINT `FK5co06xpjfujbo5mowlrjxl3v2` FOREIGN KEY (`assessment_lesson_lesson_id`) REFERENCES `assignment_lesson` (`lesson_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK5rx7iny242lh7vjj161bmlijx` FOREIGN KEY (`video_lesson_lesson_id`) REFERENCES `video_lesson` (`lesson_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKib5n1vxta6i3xe497v5irfoq0` FOREIGN KEY (`quiz_lesson_lesson_id`) REFERENCES `quiz_lesson` (`lesson_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKl7uog042qrnneaje24n3vc10x` FOREIGN KEY (`section_id`) REFERENCES `section` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKnuw5imgpfqo32319gugcu0wev` FOREIGN KEY (`article_lesson_lesson_id`) REFERENCES `article_lesson` (`lesson_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `lesson_chk_1` CHECK (`lecture_type` between 0 and 3)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of lesson
-- ----------------------------

-- ----------------------------
-- Table structure for quiz_lesson
-- ----------------------------
DROP TABLE IF EXISTS `quiz_lesson`;
CREATE TABLE `quiz_lesson`  (
  `lesson_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `end_time` datetime(6) NULL DEFAULT NULL,
  `start_time` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`lesson_id`) USING BTREE,
  CONSTRAINT `FKsir9yuo4c4vfooe2eiixg69wx` FOREIGN KEY (`lesson_id`) REFERENCES `lesson` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quiz_lesson
-- ----------------------------

-- ----------------------------
-- Table structure for quiz_lesson_questions
-- ----------------------------
DROP TABLE IF EXISTS `quiz_lesson_questions`;
CREATE TABLE `quiz_lesson_questions`  (
  `quiz_lesson_lesson_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `questions_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  UNIQUE INDEX `UK115w9dl6bofo85v7y91g9spyq`(`questions_id` ASC) USING BTREE,
  INDEX `FKd923gunnlotttwdvump1rfucp`(`quiz_lesson_lesson_id` ASC) USING BTREE,
  CONSTRAINT `FKd923gunnlotttwdvump1rfucp` FOREIGN KEY (`quiz_lesson_lesson_id`) REFERENCES `quiz_lesson` (`lesson_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKq7oi25hy0mhs2ljs0d92oufs7` FOREIGN KEY (`questions_id`) REFERENCES `quiz_question` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quiz_lesson_questions
-- ----------------------------

-- ----------------------------
-- Table structure for quiz_option
-- ----------------------------
DROP TABLE IF EXISTS `quiz_option`;
CREATE TABLE `quiz_option`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `version` bigint NOT NULL,
  `is_correct` bit(1) NULL DEFAULT NULL,
  `option_order` int NULL DEFAULT NULL,
  `option_text` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `question_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK2i5dvgyy8wygpkiy9dwcfwoao`(`question_id` ASC) USING BTREE,
  CONSTRAINT `FK2i5dvgyy8wygpkiy9dwcfwoao` FOREIGN KEY (`question_id`) REFERENCES `quiz_question` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quiz_option
-- ----------------------------

-- ----------------------------
-- Table structure for quiz_question
-- ----------------------------
DROP TABLE IF EXISTS `quiz_question`;
CREATE TABLE `quiz_question`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `version` bigint NOT NULL,
  `order_index` int NULL DEFAULT NULL,
  `question_text` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `question_type` tinyint NULL DEFAULT NULL,
  `weight` smallint NULL DEFAULT NULL,
  `quiz_lesson_lesson_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKlatvvxg50mg916qvmpcv2otdw`(`quiz_lesson_lesson_id` ASC) USING BTREE,
  CONSTRAINT `FKlatvvxg50mg916qvmpcv2otdw` FOREIGN KEY (`quiz_lesson_lesson_id`) REFERENCES `quiz_lesson` (`lesson_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `quiz_question_chk_1` CHECK (`question_type` between 0 and 4)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of quiz_question
-- ----------------------------

-- ----------------------------
-- Table structure for section
-- ----------------------------
DROP TABLE IF EXISTS `section`;
CREATE TABLE `section`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `duration` bigint NULL DEFAULT NULL,
  `order_index` int NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `course_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `version` bigint NOT NULL,
  `created_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `updated_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKoy8uc0ftpivwopwf5ptwdtar0`(`course_id` ASC) USING BTREE,
  CONSTRAINT `FKoy8uc0ftpivwopwf5ptwdtar0` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of section
-- ----------------------------
INSERT INTO `section` VALUES ('76b692e4-4f8c-44f6-a129-9e1c25b1f282', 'OK', 30, 2, 'Chuong 2', '97c88c68-09e2-4ce4-a129-b3b12c534637', 0, NULL, NULL, NULL, NULL);
INSERT INTO `section` VALUES ('97dffacd-eef1-4245-aaaf-38ccb239d9da', 'OK', 30, 3, 'Chuong 3', '97c88c68-09e2-4ce4-a129-b3b12c534637', 0, NULL, NULL, NULL, NULL);
INSERT INTO `section` VALUES ('cc9e6d3f-7ce6-4aa9-903e-62d13095642c', 'OK', 30, 1, 'Chuong 1', '97c88c68-09e2-4ce4-a129-b3b12c534637', 0, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for section_lectures
-- ----------------------------
DROP TABLE IF EXISTS `section_lectures`;
CREATE TABLE `section_lectures`  (
  `section_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `lectures_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`section_id`, `lectures_id`) USING BTREE,
  UNIQUE INDEX `UK1hes6wup5c5kiaptcfkwosmqv`(`lectures_id` ASC) USING BTREE,
  CONSTRAINT `FKa0jy3y6c2xrrm8dikjrv66eyv` FOREIGN KEY (`lectures_id`) REFERENCES `lecture` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKt1e7yqe1jqug5w1x9ralt652b` FOREIGN KEY (`section_id`) REFERENCES `section` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of section_lectures
-- ----------------------------
INSERT INTO `section_lectures` VALUES ('76b692e4-4f8c-44f6-a129-9e1c25b1f282', '19c199ba-8934-4510-a861-ddf7da364aa9');
INSERT INTO `section_lectures` VALUES ('97dffacd-eef1-4245-aaaf-38ccb239d9da', '203f66e3-c03c-4bf7-b5f3-1fc627f8fcb0');
INSERT INTO `section_lectures` VALUES ('76b692e4-4f8c-44f6-a129-9e1c25b1f282', '44b3f429-5b90-40fe-8558-20a6ac35bc8f');
INSERT INTO `section_lectures` VALUES ('76b692e4-4f8c-44f6-a129-9e1c25b1f282', '5a47e8da-fdad-49af-9469-5870aaadeba3');
INSERT INTO `section_lectures` VALUES ('cc9e6d3f-7ce6-4aa9-903e-62d13095642c', '7ad62a16-0e06-4863-ad27-1abdc332aef7');
INSERT INTO `section_lectures` VALUES ('cc9e6d3f-7ce6-4aa9-903e-62d13095642c', '7fd93065-d3a0-49ad-8e2c-a1a44572d792');
INSERT INTO `section_lectures` VALUES ('cc9e6d3f-7ce6-4aa9-903e-62d13095642c', '83405274-ba1d-4099-a11d-7baffb6701e4');
INSERT INTO `section_lectures` VALUES ('97dffacd-eef1-4245-aaaf-38ccb239d9da', 'cbe798c9-6fab-455a-aa44-a393806b3473');
INSERT INTO `section_lectures` VALUES ('97dffacd-eef1-4245-aaaf-38ccb239d9da', 'fc44b3b2-abc7-4def-b26c-67a38701b7b0');

-- ----------------------------
-- Table structure for section_lessons
-- ----------------------------
DROP TABLE IF EXISTS `section_lessons`;
CREATE TABLE `section_lessons`  (
  `section_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `lessons_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  UNIQUE INDEX `UK4x59bkllg6k5fjkm87nbwwlls`(`lessons_id` ASC) USING BTREE,
  INDEX `FKhexv93xrn789wl76f6ns321qy`(`section_id` ASC) USING BTREE,
  CONSTRAINT `FK6k4xnl4iup2tqx90g0dj610u7` FOREIGN KEY (`lessons_id`) REFERENCES `lesson` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKhexv93xrn789wl76f6ns321qy` FOREIGN KEY (`section_id`) REFERENCES `section` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of section_lessons
-- ----------------------------

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tag
-- ----------------------------

-- ----------------------------
-- Table structure for tag_courses
-- ----------------------------
DROP TABLE IF EXISTS `tag_courses`;
CREATE TABLE `tag_courses`  (
  `tag_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `courses_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`tag_id`, `courses_id`) USING BTREE,
  INDEX `FKbsb69mxgm0hb6wmela8wjbt23`(`courses_id` ASC) USING BTREE,
  CONSTRAINT `FKajcthrpbia0ng38sibxyxkogy` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKbsb69mxgm0hb6wmela8wjbt23` FOREIGN KEY (`courses_id`) REFERENCES `course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tag_courses
-- ----------------------------

-- ----------------------------
-- Table structure for video_lesson
-- ----------------------------
DROP TABLE IF EXISTS `video_lesson`;
CREATE TABLE `video_lesson`  (
  `lesson_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `video_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`lesson_id`) USING BTREE,
  CONSTRAINT `FKit1assc4y184uhpx7g75sj95d` FOREIGN KEY (`lesson_id`) REFERENCES `lesson` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of video_lesson
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
