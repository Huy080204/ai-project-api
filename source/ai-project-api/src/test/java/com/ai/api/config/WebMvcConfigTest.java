package com.ai.api.config;

import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

class WebMvcConfigTest {

    @Data
    static class Sample {
        private Long id = 7L;
        private String name = "rule";
        private String nullValue = null;
        private String emptyValue = "";
        private List<String> emptyList = Collections.emptyList();
        private Date createdDate = new Date(0L);
        private LocalDate day = LocalDate.of(2026, 9, 28);
        private LocalDateTime at = LocalDateTime.of(2026, 9, 28, 13, 45, 30);
    }

    @Test
    void shouldWriteJsonWithProjectDateFormatsAndWithoutEmptyFieldsWhenUsingJsonConverter() throws Exception {
        String json = writeJson();

        assertThat(json.replaceAll("\\s", ""))
                .isEqualTo("{\"id\":7,\"name\":\"rule\",\"createdDate\":\"01/01/197000:00:00\","
                        + "\"day\":\"28/09/2026\",\"at\":\"28/09/202613:45:30\"}");
    }

    @Test
    void shouldPutByteArrayConverterBeforeJsonConverterWhenConfiguringMessageConverters() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        new WebMvcConfig().configureMessageConverters(converters);

        assertThat(converters.get(0)).isInstanceOf(ByteArrayHttpMessageConverter.class);
        assertThat(converters).hasAtLeastOneElementOfType(MappingJackson2HttpMessageConverter.class);
    }

    @SuppressWarnings("unchecked")
    private String writeJson() throws Exception {
        TimeZone previous = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try {
            List<HttpMessageConverter<?>> converters = new ArrayList<>();
            new WebMvcConfig().configureMessageConverters(converters);
            HttpMessageConverter<Object> converter = (HttpMessageConverter<Object>) converters.stream()
                    .filter(MappingJackson2HttpMessageConverter.class::isInstance).findFirst()
                    .orElseThrow(IllegalStateException::new);
            MockHttpOutputMessage output = new MockHttpOutputMessage();
            converter.write(new Sample(), MediaType.APPLICATION_JSON, output);
            return output.getBodyAsString();
        } finally {
            TimeZone.setDefault(previous);
        }
    }
}
