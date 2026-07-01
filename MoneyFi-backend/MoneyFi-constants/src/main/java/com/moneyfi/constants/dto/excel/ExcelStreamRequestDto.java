package com.moneyfi.constants.dto.excel;

import java.util.stream.Stream;

public class ExcelStreamRequestDto<T> {
    private String fileName;
    private String sheetName;
    private Class<T> classType;
    private Stream<T> dataStream;

    public ExcelStreamRequestDto() {
    }

    public ExcelStreamRequestDto(String fileName, String sheetName, Class<T> classType, Stream<T> dataStream) {
        this.fileName = fileName;
        this.sheetName = sheetName;
        this.classType = classType;
        this.dataStream = dataStream;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public Class<T> getClassType() {
        return classType;
    }

    public void setClassType(Class<T> classType) {
        this.classType = classType;
    }

    public Stream<T> getDataStream() {
        return dataStream;
    }

    public void setDataStream(Stream<T> dataStream) {
        this.dataStream = dataStream;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private final ExcelStreamRequestDto<T> dto = new ExcelStreamRequestDto<>();

        public Builder<T> fileName(String fileName) {
            dto.fileName = fileName;
            return this;
        }

        public Builder<T> sheetName(String sheetName) {
            dto.sheetName = sheetName;
            return this;
        }

        public Builder<T> classType(Class<T> classType) {
            dto.classType = classType;
            return this;
        }

        public Builder<T> dataStream(Stream<T> dataStream) {
            dto.dataStream = dataStream;
            return this;
        }

        public ExcelStreamRequestDto<T> build() {
            return dto;
        }
    }
}
