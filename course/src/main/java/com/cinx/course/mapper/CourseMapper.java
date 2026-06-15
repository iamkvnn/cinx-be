package com.cinx.course.mapper;

import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.request.UpdateCourseRequest;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.dto.response.UserDto;
import com.cinx.course.model.Course;
import com.cinx.course.model.CourseDraft;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "discountRate", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "enrollmentCount", ignore = true)
    @Mapping(target = "instructorId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "publishStatus", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    Course toModel(CreateCourseRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "discountRate", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "enrollmentCount", ignore = true)
    @Mapping(target = "instructorId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "publishStatus", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    void partialUpdate(@MappingTarget Course course, UpdateCourseRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "course", source = ".")
    CourseDraft toDraft(Course course);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "discountRate", ignore = true)
    @Mapping(target = "category", ignore = true)
    void partialUpdate(@MappingTarget CourseDraft draft, UpdateCourseRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "enrollmentCount", ignore = true)
    @Mapping(target = "instructorId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "publishStatus", ignore = true)
    @Mapping(target = "images", ignore = true)
    void copyDraftToCourse(CourseDraft draft, @MappingTarget Course course);

    @Mapping(target = "instructor", source = "instructor")
    @Mapping(target = "instructor.id", source = "instructor.userId")
    CourseResponse toResponse(Course course, UserDto instructor);

    @Mapping(target = "id", source = "course.id")
    @Mapping(target = "title", source = "draft.title")
    @Mapping(target = "description", source = "draft.description")
    @Mapping(target = "category", source = "draft.category")
    @Mapping(target = "instructor", source = "instructor")
    @Mapping(target = "instructor.id", source = "instructor.userId")
    @Mapping(target = "images", source = "course.images")
    @Mapping(target = "price", source = "draft.price")
    @Mapping(target = "discountedPrice", source = "draft.discountedPrice")
    @Mapping(target = "discountRate", source = "draft.discountRate")
    @Mapping(target = "rating", source = "course.rating")
    @Mapping(target = "enrollmentCount", source = "course.enrollmentCount")
    @Mapping(target = "isInSubscription", source = "draft.isInSubscription")
    @Mapping(target = "duration", source = "draft.duration")
    @Mapping(target = "hasCertificate", source = "draft.hasCertificate")
    @Mapping(target = "certificateTitle", source = "draft.certificateTitle")
    @Mapping(target = "status", source = "course.status")
    @Mapping(target = "publishStatus", source = "course.publishStatus")
    @Mapping(target = "createdAt", source = "draft.createdAt")
    @Mapping(target = "updatedAt", source = "draft.updatedAt")
    CourseResponse toResponse(Course course, CourseDraft draft, UserDto instructor);
}
