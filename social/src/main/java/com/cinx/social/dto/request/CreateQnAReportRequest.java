package com.cinx.social.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateQnAReportRequest {
    @NotBlank
    private String reason;
}
