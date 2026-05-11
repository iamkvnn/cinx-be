package com.cinx.course.dto.request;

import com.cinx.common.validation.FieldLessThan;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@FieldLessThan(message = "Discounted price must be less than original price")
public record CreateCourseRequest (
        @NotBlank(message = "Title is required")
        @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
        String title,
        
        @NotBlank(message = "Description is required")
        String description,
        
        @NotBlank(message = "Category ID is required")
        String categoryId,
        
        @NotNull(message = "Price is required")
        @Min(value = 0, message = "Price cannot be negative")
        Long price,
        
        @Min(value = 0, message = "Discounted price cannot be negative")
        Long discountedPrice,
        
        @NotNull(message = "Subscription inclusion flag is required")
        Boolean isInSubscription,
        
        @Min(value = 0, message = "Duration cannot be negative")
        Long duration,
        
        @NotNull(message = "Certificate flag is required")
        Boolean hasCertificate,
        
        String certificateTitle
) {
}
