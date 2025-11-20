package com.moneyfi.user.service.common;

public interface CommonService {
    boolean sendAccountStatementEmail(String username, Long userId, byte[] pdfBytes);

    boolean sendSpendingAnalysisEmail(String username, Long userId, byte[] pdfBytes);
}
