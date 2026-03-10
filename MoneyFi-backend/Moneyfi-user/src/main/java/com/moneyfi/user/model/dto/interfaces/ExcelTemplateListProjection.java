package com.moneyfi.user.model.dto.interfaces;

import java.time.LocalDateTime;

public interface ExcelTemplateListProjection {
    String getExcelType();
    byte[] getExcelFile();
    String getCreatedBy();
    String getUpdatedBy();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}
