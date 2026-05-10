package com.cinx.learning.service.quiz;

import com.cinx.learning.consts.QuizQuestionType;
import com.cinx.learning.dto.response.QuizOptionResponse;
import com.cinx.learning.dto.response.QuizQuestionResponse;
import com.cinx.learning.dto.response.QuizSessionOptionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizSnapshotBuilder {

    private final ObjectMapper objectMapper;

    public String buildCorrectAnswer(QuizQuestionResponse q) {
        try {
            return switch (q.questionType()) {
                case ORDERING -> objectMapper.writeValueAsString(
                        q.options().stream()
                                .filter(o -> Boolean.TRUE.equals(o.isCorrect()))
                                .sorted(Comparator.comparing(QuizOptionResponse::optionOrder,
                                        Comparator.nullsLast(Comparator.naturalOrder())))
                                .map(QuizOptionResponse::id)
                                .toList());
                case MATCHING -> objectMapper.writeValueAsString(
                        q.options().stream()
                                .filter(o -> Boolean.TRUE.equals(o.isCorrect()))
                                .sorted(Comparator.comparing(QuizOptionResponse::optionOrder,
                                        Comparator.nullsLast(Comparator.naturalOrder())))
                                .map(o -> Map.of("optionId", o.id(), "matchText", o.matchText()))
                                .toList());
                case SHORT_TEXT, ESSAY -> objectMapper.writeValueAsString(
                        q.options().stream()
                                .filter(o -> Boolean.TRUE.equals(o.isCorrect()))
                                .map(QuizOptionResponse::optionText)
                                .sorted()
                                .toList());
                default -> // SINGLE_CHOICE, MULTI_CHOICE
                        objectMapper.writeValueAsString(
                                q.options().stream()
                                        .filter(o -> Boolean.TRUE.equals(o.isCorrect()))
                                        .map(QuizOptionResponse::id)
                                        .sorted()
                                        .toList());
            };
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize correct answer to JSON", e);
        }
    }

    public String buildOptionsSnapshot(QuizQuestionResponse q, boolean isShuffle) {
        try {
            List<QuizOptionResponse> options = new ArrayList<>(q.options());
            if (isShuffle) {
                Collections.shuffle(options);
            }

            if (q.questionType() == QuizQuestionType.MATCHING) {
                List<QuizSessionOptionResponse> leftItems = options.stream()
                        .map(o -> new QuizSessionOptionResponse(
                                o.id(), o.optionText(), "LEFT"))
                        .toList();

                List<String> shuffledTexts = new ArrayList<>(options.stream()
                        .map(QuizOptionResponse::matchText)
                        .toList());
                Collections.shuffle(shuffledTexts);
                List<QuizSessionOptionResponse> rightItems = shuffledTexts.stream()
                        .map(text -> new QuizSessionOptionResponse(null, text, "RIGHT"))
                        .toList();

                List<QuizSessionOptionResponse> combined = new ArrayList<>(leftItems);
                combined.addAll(rightItems);
                return objectMapper.writeValueAsString(combined);
            }

            List<QuizSessionOptionResponse> items = new ArrayList<>(options.stream()
                    .map(o -> new QuizSessionOptionResponse(
                            o.id(), o.optionText(), null))
                    .toList());
            return objectMapper.writeValueAsString(items);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize options snapshot", e);
        }
    }

    public List<QuizSessionOptionResponse> parseOptionsSnapshot(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse optionsSnapshot: {}", e.getMessage());
            return List.of();
        }
    }
}
