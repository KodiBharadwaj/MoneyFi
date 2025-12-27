package com.moneyfi.transaction.service.admin;

import com.moneyfi.transaction.service.admin.dto.response.TransactionCategoryResponse;

public interface AdminService {
    TransactionCategoryResponse getCategoryWiseTransactionSummary();
}
