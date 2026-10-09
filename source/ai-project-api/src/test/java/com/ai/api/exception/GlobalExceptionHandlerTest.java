package com.ai.api.exception;

import com.ai.api.dto.ApiMessageDto;
import com.ai.api.form.ErrorForm;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHideRawMessageWhenUnexpectedExceptionIsThrown() {
        Exception ex = new IllegalStateException("could not execute query: select * from db_account");

        ApiMessageDto<List<ErrorForm>> result = handler.exceptionHandler(ex);

        assertThat(result.getResult()).isFalse();
        assertThat(result.getCode()).isEqualTo("ERROR");
        assertThat(result.getMessage()).isEqualTo("[Ex2]: Internal error");
    }

    @Test
    void shouldReturnFormErrorsWhenBindingExceptionIsThrown() {
        Exception ex = new MyBindingException("[{\"field\":\"name\",\"message\":\"must not be blank\"}]");

        ApiMessageDto<List<ErrorForm>> result = handler.exceptionHandler(ex);

        assertThat(result.getMessage()).isEqualTo("Invalid form");
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().get(0).getField()).isEqualTo("name");
        assertThat(result.getData().get(0).getMessage()).isEqualTo("must not be blank");
    }
}
