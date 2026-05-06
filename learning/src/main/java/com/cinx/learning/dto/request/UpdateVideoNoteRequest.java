package com.cinx.learning.dto.request;

import lombok.Data;

@Data
public class UpdateVideoNoteRequest {
    private String content;
    private Integer videoTimestamp;
}
