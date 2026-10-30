package com.dreamteam.breakloop.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/** Same alphabet (no I, O, 0, 1) and length as the Flutter app. */
@Component
public class FriendCodeGenerator {

    public static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    public static final int LENGTH = 4;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder code = new StringBuilder(LENGTH);

        for (int i = 0; i < LENGTH; i++) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }

        return code.toString();
    }
}
