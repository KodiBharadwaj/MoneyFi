package com.moneyfi.constants.dto;

public class ExcelResponseDto {

    private byte[] excelBytes;
    private String excelName;

    public ExcelResponseDto() {
    }

    public ExcelResponseDto(byte[] excelBytes, String excelName) {
        this.excelBytes = excelBytes;
        this.excelName = excelName;
    }

    public byte[] getExcelBytes() {
        return excelBytes;
    }

    public void setExcelBytes(byte[] excelBytes) {
        this.excelBytes = excelBytes;
    }

    public String getExcelName() {
        return excelName;
    }

    public void setExcelName(String excelName) {
        this.excelName = excelName;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private byte[] excelBytes;
        private String excelName;

        public Builder excelBytes(byte[] excelBytes) {
            this.excelBytes = excelBytes;
            return this;
        }

        public Builder excelName(String excelName) {
            this.excelName = excelName;
            return this;
        }

        public ExcelResponseDto build() {
            return new ExcelResponseDto(excelBytes, excelName);
        }
    }

}
