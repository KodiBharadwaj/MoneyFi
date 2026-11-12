package com.moneyfi.user.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.moneyfi.constants.constants.BasePathConstants.USER_SERVICE_BASE_PATH;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(USER_SERVICE_BASE_PATH, handlerType -> Boolean.TRUE);
    }
}
