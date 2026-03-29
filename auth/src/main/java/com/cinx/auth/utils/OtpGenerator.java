package com.cinx.auth.utils;

import java.util.Random;

public class OtpGenerator {
    public static String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}
