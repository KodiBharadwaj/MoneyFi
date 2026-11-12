package com.moneyfi.wealthcore.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.moneyfi.constants.constants.CommonConstants.*;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = STRING_BEARER_AUTH;
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .servers(List.of(
                        new Server().url(SLASH)
                ))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(AUTHORIZATION)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme(BEARER.toLowerCase())
                                        .bearerFormat(STRING_JWT)
                        )
                );
    }
}
