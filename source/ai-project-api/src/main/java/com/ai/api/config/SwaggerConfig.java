package com.ai.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Collections;

@Configuration
@Profile({"local", "dev", "staging"})
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().servers(Collections.singletonList(new Server().url("/")));
    }

    @Bean
    public GroupedOpenApi storeAuthApi() {
        return GroupedOpenApi.builder()
                .group("storeAuthApi")
                .packagesToScan("com.ai.api.controller")
                .build();
    }
}
