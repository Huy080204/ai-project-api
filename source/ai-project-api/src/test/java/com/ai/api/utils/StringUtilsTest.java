package com.ai.api.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsTest {

    @Test
    void shouldReturnLowercaseAlphanumericOfGivenLengthWhenGeneratingRandomString() {
        String result = StringUtils.generateRandomString(32);

        assertThat(result).hasSize(32);
        assertThat(result).matches("[a-z0-9]{32}");
    }

    @Test
    void shouldReturnEmptyStringWhenLengthIsZero() {
        String result = StringUtils.generateRandomString(0);

        assertThat(result).isEmpty();
    }
}
