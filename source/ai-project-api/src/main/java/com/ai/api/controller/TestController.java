package com.ai.api.controller;

import com.ai.api.dto.ApiMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/v1")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class TestController extends ABasicController {

    @GetMapping(value = "/test-error", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> testError() {
        log.error("Test error log at {}", Instant.now());
        return makeSuccessResponse("Test error log triggered");
    }

    @GetMapping(value = "/test-out-of-memory", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> testOutOfMemory() {
        log.error("OutOfMemory error log at {}", Instant.now());
        return makeSuccessResponse("OutOfMemory error log triggered");
    }

    @GetMapping(value = "/test-device-not-found", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> testDeviceNotFound() {
        log.error("Device not found error log at {}", Instant.now());
        return makeSuccessResponse("Device not found error log triggered");
    }

    @GetMapping(value = "/test-slow-response", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> testSlowResponse(@RequestParam(value = "sleepMs", defaultValue = "300") long sleepMs) throws InterruptedException {
        Thread.sleep(sleepMs);
        log.warn("Slow response test triggered, sleepMs={} at {}", sleepMs, Instant.now());
        return makeSuccessResponse("Slow response log triggered, sleepMs=" + sleepMs);
    }
}
