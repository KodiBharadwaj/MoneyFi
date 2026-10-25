package com.moneyfi.user.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.moneyfi.constants.constants.CommonConstants.AUTHORIZATION;
import static com.moneyfi.constants.constants.CommonConstants.BEARER;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .servers(List.of(
                        new Server().url("/")
                ))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(AUTHORIZATION)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme(BEARER.toLowerCase())
                                        .bearerFormat("JWT")
                        )
                );
    }
}
