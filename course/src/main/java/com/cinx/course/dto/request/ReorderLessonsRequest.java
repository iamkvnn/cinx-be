package com.cinx.course.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReorderLessonsRequest(
        @NotNull
        List<@Valid SectionLessonsOrderRequest> sections
) {
}
