package com.moneyfi.constants.service;

import com.moneyfi.constants.dto.excel.ExcelStreamRequestDto;

import java.io.OutputStream;

public interface ExcelGenerationService {
    <T> void generateExcelReport(ExcelStreamRequestDto<T> requestDto, OutputStream outputStream);
}
