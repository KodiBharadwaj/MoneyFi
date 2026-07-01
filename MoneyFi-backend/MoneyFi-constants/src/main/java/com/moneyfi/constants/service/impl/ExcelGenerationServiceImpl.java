package com.moneyfi.constants.service.impl;

import com.moneyfi.constants.annotation.ExcelColumn;
import com.moneyfi.constants.annotation.ExcelSheet;
import com.moneyfi.constants.constants.CommonConstants;
import com.moneyfi.constants.dto.excel.ExcelStreamRequestDto;
import com.moneyfi.constants.enums.ExcelWidthType;
import com.moneyfi.constants.service.ExcelGenerationService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ExcelGenerationServiceImpl implements ExcelGenerationService {

    @Override
    public <T> void generateExcelReport(ExcelStreamRequestDto<T> request, OutputStream outputStream) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(getSheetName(request));

            CellStyle headerStyle = createHeaderStyle(workbook);

            List<Field> fields = getExcelFields(request.getClassType());

            writeHeader(sheet, fields, headerStyle);
            writeData(sheet, request.getDataStream(), fields, workbook);
            autoSizeColumns(sheet, fields);

            workbook.write(outputStream);

        } catch (Exception ex) {
            throw new RuntimeException("Unable to generate excel.", ex);
        }
    }

    private String getSheetName(ExcelStreamRequestDto<?> request) {
        if (request.getSheetName() != null) {
            return request.getSheetName();
        }
        ExcelSheet sheet = request.getClassType().getAnnotation(ExcelSheet.class);
        return sheet != null ? sheet.name() : "Sheet1";
    }

    private List<Field> getExcelFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ExcelColumn.class) && !field.getAnnotation(ExcelColumn.class).ignore())
                .sorted(Comparator.comparingInt(field -> field.getAnnotation(ExcelColumn.class).order()))
                .peek(field -> field.setAccessible(true))
                .toList();
    }

    private void writeHeader(Sheet sheet, List<Field> fields, CellStyle style) {
        Row row = sheet.createRow(0);
        int column = 0;
        for (Field field : fields) {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);

            Cell cell = row.createCell(column);
            cell.setCellValue(annotation.header());
            cell.setCellStyle(style);

            if (annotation.widthType() == ExcelWidthType.STATIC) {
                sheet.setColumnWidth(column, annotation.width() * 256);
            } else if (sheet instanceof SXSSFSheet sxssfSheet) {
                sxssfSheet.trackColumnForAutoSizing(column);
            }

            column++;
        }
    }

    private <T> void writeData(Sheet sheet, Stream<T> stream, List<Field> fields, SXSSFWorkbook workbook) {
        AtomicInteger rowIndex = new AtomicInteger(1);

        stream.forEach(object -> {
            Row row = sheet.createRow(rowIndex.getAndIncrement());

            int column = 0;
            for (Field field : fields) {
                Cell cell = row.createCell(column++);
                try {
                    Object value = field.get(object);
                    writeCell(cell, value, field.getAnnotation(ExcelColumn.class).dateFormat(), workbook);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void writeCell(Cell cell, Object value, String dateFormat, SXSSFWorkbook workbook) {
        if (value == null) {
            cell.setCellValue("-");
            return;
        }
        if (value instanceof String str) {
            cell.setCellValue(str);
        }
        else if (value instanceof Integer n) {
            cell.setCellValue(n);
        }
        else if (value instanceof Long n) {
            cell.setCellValue(n);
        }
        else if (value instanceof Double n) {
            cell.setCellValue(n);
        }
        else if (value instanceof Boolean b) {
            cell.setCellValue(b);
        }
        else if (value instanceof Date date) {
            cell.setCellValue(date);
            cell.setCellStyle(getDateStyle(workbook, dateFormat));
        }
        else if (value instanceof LocalDate date) {
            cell.setCellValue(date);
            cell.setCellStyle(getDateStyle(workbook, dateFormat));
        }
        else if (value instanceof LocalDateTime date) {
            cell.setCellValue(date);
            cell.setCellStyle(getDateStyle(workbook, dateFormat));
        }
        else {
            cell.setCellValue(value.toString());
        }
    }

    private void autoSizeColumns(Sheet sheet, List<Field> fields) {
        for (int column = 0; column < fields.size(); column++) {
            ExcelColumn annotation = fields.get(column).getAnnotation(ExcelColumn.class);
            if (annotation.widthType() == ExcelWidthType.DYNAMIC) {
                sheet.autoSizeColumn(column);
            }
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle getDateStyle(Workbook workbook, String format) {
        return CommonConstants.dateStyles.computeIfAbsent(format, f -> {
            CellStyle style = workbook.createCellStyle();
            style.setDataFormat(
                    workbook.getCreationHelper()
                            .createDataFormat()
                            .getFormat(f));
            return style;
        });
    }
}
