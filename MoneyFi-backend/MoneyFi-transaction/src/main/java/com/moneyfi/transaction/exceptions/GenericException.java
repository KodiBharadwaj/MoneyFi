package com.moneyfi.transaction.exceptions;

import com.moneyfi.transaction.service.transaction.dto.response.GmailSyncErrorResponse;

import java.util.List;

public class GenericException extends Exception {
    private List<GmailSyncErrorResponse> errorList;

    private GenericException(List<GmailSyncErrorResponse> errorList) {
        super("Errors occurred during Gmail sync");
        this.errorList = errorList;
    }

    public List<GmailSyncErrorResponse> getErrorList() {
        return errorList;
    }

    public static GenericException create(List<GmailSyncErrorResponse> errorList) {
        return new GenericException(errorList);
    }
}
