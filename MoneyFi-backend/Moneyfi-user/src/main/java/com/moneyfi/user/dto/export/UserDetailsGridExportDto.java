package com.moneyfi.user.dto.export;

import com.moneyfi.constants.annotation.ExcelColumn;
import com.moneyfi.constants.annotation.ExcelSheet;
import lombok.Builder;

import java.sql.Timestamp;
import java.util.Date;

@Builder
@ExcelSheet(name = "User Grid Details")
public class UserDetailsGridExportDto {

    @ExcelColumn(header = "Sl No")
    private int slNo;

    @ExcelColumn(header = "Name of User")
    private String name;

    @ExcelColumn(header = "username")
    private String username;

    @ExcelColumn(header = "Phone Number")
    private String phone;

    @ExcelColumn(header = "Date of Creation")
    private Timestamp createdDateTime;

    @ExcelColumn(header = "Date of Birth")
    private Date dateOfBirth;
}
