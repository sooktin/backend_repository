package com.sooktin.backend.global;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info());
    }

    private Info apiInfo() {
        return new Info()
                .title("Sooktin Swagger")
                .description("숙틴 v1 명세서입니다")
                .version("1.0");
    }
}
