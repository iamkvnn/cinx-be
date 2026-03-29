package com.cinx.learning.dto.request;

import java.util.List;

public record SubmitQuizSessionRequest(
        List<ChooseQuizAnswerRequest> answers
) {
}
