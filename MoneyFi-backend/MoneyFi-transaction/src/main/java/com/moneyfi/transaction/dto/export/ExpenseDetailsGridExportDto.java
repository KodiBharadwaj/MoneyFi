package com.moneyfi.transaction.dto.export;

import com.moneyfi.constants.annotation.ExcelColumn;
import com.moneyfi.constants.annotation.ExcelSheet;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Date;

@Builder
@ExcelSheet(name = "Expense Grid Details")
public class ExpenseDetailsGridExportDto {

    @ExcelColumn(header = "Expense Id", order = 1)
    private Long id;

    @ExcelColumn(header = "Category", order = 2)
    private String category;

    @ExcelColumn(header = "Amount", order = 3)
    private BigDecimal amount;

    @ExcelColumn(header = "Date", order = 4)
    private Date date;

    @ExcelColumn(header = "Is Recurring", order = 5)
    private boolean recurring;

    @ExcelColumn(header = "Description", order = 6)
    private String description;

    @ExcelColumn(header = "Is Deleted", order = 6)
    private boolean deleted;

}
