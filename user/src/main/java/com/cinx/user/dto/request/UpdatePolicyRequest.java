package com.cinx.user.dto.request;

import com.cinx.user.consts.PolicyType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record UpdatePolicyRequest(
        PolicyType policyType,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 1000) String summary,
        LocalDateTime effectiveAt,
        Integer displayOrder,
        @Valid @NotEmpty List<PolicySectionRequest> sections
) {
}
