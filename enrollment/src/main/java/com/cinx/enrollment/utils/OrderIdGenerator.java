package com.cinx.enrollment.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
public class OrderIdGenerator {
    private static final String PREFIX = "CINX";
    private static final int RANDOM_LENGTH = 6;
    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final Random random = new Random();

    public String generateCode() {
        DateTimeFormatter formatter
                = DateTimeFormatter.ofPattern(
                "yyMMddHHmm");
        String timeStamp = LocalDateTime.now().format(formatter);

        StringBuilder randomPart = new StringBuilder();
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            int index = random.nextInt(CHAR_POOL.length());
            randomPart.append(CHAR_POOL.charAt(index));
        }

        return PREFIX + timeStamp + randomPart;
    }
}
