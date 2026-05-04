package com.cinx.learning.service.quiz.evaluator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for parsing the canonical answer format stored as strings.
 *
 * <p><b>Contract</b>:
 * <ul>
 *   <li>Single/Multi/Ordering – comma-separated option IDs, e.g. {@code "optA,optB"}</li>
 *   <li>Matching – comma-separated {@code "optionId:matchText"} pairs, e.g. {@code "opt1:Paris,opt2:Berlin"}</li>
 *   <li>Short text / Essay – raw string, single element list</li>
 * </ul>
 */
final class AnswerParser {

    private AnswerParser() {}

    /** Split a comma-separated answer string into an ordered list of tokens. */
    static List<String> parseList(String answer) {
        if (answer == null || answer.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(answer.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
