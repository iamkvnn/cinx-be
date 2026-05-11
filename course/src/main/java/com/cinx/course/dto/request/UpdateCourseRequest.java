package com.cinx.course.dto.request;

import com.cinx.common.validation.FieldLessThan;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@FieldLessThan(message = "Discounted price must be less than original price")
public record UpdateCourseRequest(
        @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
        String title,
        
        String description,
        
        String categoryId,
        
        @Min(value = 0, message = "Price cannot be negative")
        Long price,
        
        @Min(value = 0, message = "Discounted price cannot be negative")
        Long discountedPrice,
        
        Boolean isInSubscription,
        
        @Min(value = 0, message = "Duration cannot be negative")
        Long duration,
        
        Boolean hasCertificate,
        
        String certificateTitle
) {
}
