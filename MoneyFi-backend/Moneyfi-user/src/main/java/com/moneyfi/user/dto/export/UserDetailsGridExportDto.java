package com.moneyfi.user.dto.export;

import com.moneyfi.constants.annotation.ExcelColumn;
import com.moneyfi.constants.annotation.ExcelSheet;
import com.moneyfi.constants.enums.ExcelWidthType;
import lombok.Builder;

import java.sql.Timestamp;
import java.util.Date;

@Builder
@ExcelSheet(name = "User Grid Details")
public class UserDetailsGridExportDto {

    @ExcelColumn(header = "Sl No", order = 1)
    private int slNo;

    @ExcelColumn(header = "Name of User", order = 2, widthType = ExcelWidthType.STATIC, width = 25)
    private String name;

    @ExcelColumn(header = "username", order = 3)
    private String username;

    @ExcelColumn(header = "Phone Number", order = 4)
    private String phone;

    @ExcelColumn(header = "Date of Creation", order = 5)
    private Timestamp createdDateTime;

    @ExcelColumn(header = "Date of Birth", order = 6, dateFormat = "dd-MMM-yyyy")
    private Date dateOfBirth;
}
