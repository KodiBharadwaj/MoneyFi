package com.moneyfi.transaction.dto.export;

import com.moneyfi.constants.annotation.ExcelColumn;
import com.moneyfi.constants.annotation.ExcelSheet;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Date;

@Builder
@ExcelSheet(name = "Income Grid Details")
public class IncomeDetailsGridExportDto {

    @ExcelColumn(header = "Income Id", order = 1)
    private Long id;

    @ExcelColumn(header = "Amount", order = 2)
    private BigDecimal amount;

    @ExcelColumn(header = "Income Source", order = 3)
    private String source;

    @ExcelColumn(header = "Date", order = 4)
    private Date date;

    @ExcelColumn(header = "Category", order = 5)
    private String category;

    @ExcelColumn(header = "Is Recurring", order = 6)
    private boolean recurring;

    @ExcelColumn(header = "Description", order = 7)
    private String description;

}
