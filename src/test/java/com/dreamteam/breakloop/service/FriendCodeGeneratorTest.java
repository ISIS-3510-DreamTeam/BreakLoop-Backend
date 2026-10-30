package com.dreamteam.breakloop.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FriendCodeGeneratorTest {

    private final FriendCodeGenerator generator = new FriendCodeGenerator();

    @Test
    void alphabetExcludesAmbiguousCharacters() {
        assertEquals("ABCDEFGHJKLMNPQRSTUVWXYZ23456789",
                FriendCodeGenerator.ALPHABET);
    }

    @Test
    void codesHaveFourCharactersFromTheAlphabet() {
        for (int i = 0; i < 2_000; i++) {
            String code = generator.generate();

            assertEquals(4, code.length());
            assertTrue(code.chars().allMatch(
                    c -> FriendCodeGenerator.ALPHABET.indexOf(c) >= 0), code);
        }
    }
}
