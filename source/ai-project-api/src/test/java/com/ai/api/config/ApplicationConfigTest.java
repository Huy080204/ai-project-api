package com.ai.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationConfigTest {

    @Data
    static class Target {
        private String name;
    }

    @Data
    static class Dated {
        private Date createdDate = new Date(0L);
        private String nullValue = null;
    }

    @Test
    void shouldIgnoreUnknownPropertiesWhenReadingWithObjectMapperBean() throws Exception {
        ObjectMapper objectMapper = new ApplicationConfig().objectMapper();

        Target target = objectMapper.readValue("{\"name\":\"rule\",\"unknown\":1}", Target.class);

        assertThat(target.getName()).isEqualTo("rule");
    }

    @Test
    void shouldWriteDateWithProjectFormatAndOmitNullsWhenUsingObjectMapperBean() throws Exception {
        TimeZone previous = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try {
            ObjectMapper objectMapper = new ApplicationConfig().objectMapper();

            String json = objectMapper.writeValueAsString(new Dated());

            assertThat(json).isEqualTo("{\"createdDate\":\"01/01/1970 00:00:00\"}");
        } finally {
            TimeZone.setDefault(previous);
        }
    }

    @Test
    void shouldEncodeWithBcryptAndMatchWhenUsingEncoderBean() {
        PasswordEncoder encoder = new ApplicationConfig().encoder();

        String encoded = encoder.encode("Secret#123");

        assertThat(encoded).startsWith("{bcrypt}");
        assertThat(encoder.matches("Secret#123", encoded)).isTrue();
        assertThat(encoder.matches("wrong", encoded)).isFalse();
    }
}
