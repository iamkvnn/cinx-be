package com.cinx.course.dto.request;

import com.cinx.common.validation.FieldLessThan;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@FieldLessThan(message = "Discounted price must be less than original price")
public record UpdateCourseRequest(
        @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
        @Schema(example = "Advanced Spring Boot Microservices")
        String title,
        
        @Schema(example = "Learn advanced techniques for microservices")
        String description,
        
        @Schema(example = "cat_123")
        @NotBlank(message = "Category ID is required")
        String categoryId,
        
        @Min(value = 0, message = "Price cannot be negative")
        @Schema(example = "1200000")
        Long price,
        
        @Min(value = 0, message = "Discounted price cannot be negative")
        @Schema(example = "1000000")
        Long discountedPrice,
        
        @Schema(example = "true")
        Boolean isInSubscription,
        
        @Min(value = 0, message = "Duration cannot be negative")
        @Schema(example = "150")
        Long duration,
        
        @Schema(example = "true")
        Boolean hasCertificate,
        
        @Schema(example = "Spring Boot Expert Certificate")
        String certificateTitle
) {
}
