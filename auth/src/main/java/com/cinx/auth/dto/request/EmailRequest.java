package com.cinx.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailRequest {
    @Schema(example = "nguyenvana@gmail.com")
    private String to;
    @Schema(example = "Welcome to CINX!")
    private String subject;
    @Schema(example = "Hello, your account has been created.")
    private String body;
}
