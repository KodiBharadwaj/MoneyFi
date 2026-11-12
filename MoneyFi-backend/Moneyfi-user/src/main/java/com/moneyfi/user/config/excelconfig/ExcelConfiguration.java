package com.moneyfi.user.config.excelconfig;

import com.moneyfi.constants.service.ExcelGenerationService;
import com.moneyfi.constants.service.impl.ExcelGenerationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExcelConfiguration {
    @Bean
    public ExcelGenerationService excelGenerationService() {
        return new ExcelGenerationServiceImpl();
    }
}