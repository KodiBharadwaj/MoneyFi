package com.moneyfi.wealthcore.utils;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.moneyfi.wealthcore.service.budget.dto.response.SpendingAnalysisResponseDto;
import com.moneyfi.wealthcore.service.budget.dto.response.UserDetailsForSpendingAnalysisDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Component
public class GeneratePdfTemplate {

    @Value("${account.statement.admin.pdf.password}")
    private String adminPassword;

    public byte[] generatePdf(SpendingAnalysisResponseDto responseDto, String userPassword, UserDetailsForSpendingAnalysisDto userDetails, LocalDate fromDate, LocalDate toDate) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, out);

        // Set encryption BEFORE opening the document
        String ownerPassword = adminPassword;
        writer.setEncryption(
                userPassword.getBytes(),
                ownerPassword.getBytes(),
                PdfWriter.ALLOW_PRINTING,
                PdfWriter.ENCRYPTION_AES_128
        );

        document.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Paragraph title = new Paragraph("Spending Analysis Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        document.add(title);

        // Add user info
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        Paragraph userInfo = new Paragraph();
        userInfo.setAlignment(Element.ALIGN_LEFT);
        userInfo.setSpacingAfter(20);

        userInfo.add(new Phrase("Name: ", labelFont));
        userInfo.add(new Phrase(userDetails.getName() + "\n", valueFont));

        userInfo.add(new Phrase("Username: ", labelFont));
        userInfo.add(new Phrase(userDetails.getUsername() + "\n", valueFont));

        userInfo.add(new Phrase("Phone: ", labelFont));
        userInfo.add(new Phrase(userDetails.getPhoneNumber() + "\n", valueFont));

        userInfo.add(new Phrase("Date Range: ", labelFont));
        userInfo.add(new Phrase(fromDate + " to " + toDate + "\n", valueFont));

        userInfo.add(new Phrase("Address: ", labelFont));
        userInfo.add(new Phrase(userDetails.getAddress() + "\n", valueFont));

        document.add(userInfo);

        // ========== Income by Category ==========
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        Paragraph incomeHeader = new Paragraph("Income by Category", headerFont);
        incomeHeader.setSpacingAfter(10f);
        document.add(incomeHeader);

        PdfPTable incomeTable = new PdfPTable(2);
        incomeTable.setWidthPercentage(60);
        incomeTable.setWidths(new float[]{5f, 3f});
        addCell(incomeTable, "Category", headerFont);
        addCell(incomeTable, "Amount", headerFont);

        for (Map.Entry<String, BigDecimal> entry : responseDto.getIncomeByCategory().entrySet()) {
            addCell(incomeTable, entry.getKey(), cellFont);
            addCell(incomeTable, entry.getValue().toString(), cellFont);
        }
        document.add(incomeTable);

        // Add some spacing
        document.add(new Paragraph("\n"));


        // ========== Expense by Category ==========
        Paragraph expenseHeader = new Paragraph("Expense by Category", headerFont);
        expenseHeader.setSpacingAfter(10f);
        document.add(expenseHeader);

        PdfPTable expenseTable = new PdfPTable(2);
        expenseTable.setWidthPercentage(60);
        expenseTable.setWidths(new float[]{5f, 3f});
        addCell(expenseTable, "Category", headerFont);
        addCell(expenseTable, "Amount", headerFont);

        for (Map.Entry<String, BigDecimal> entry : responseDto.getExpenseByCategory().entrySet()) {
            addCell(expenseTable, entry.getKey(), cellFont);
            addCell(expenseTable, entry.getValue().toString(), cellFont);
        }
        document.add(expenseTable);

        // Add some spacing
        document.add(new Paragraph("\n"));

        // ========== Summary ==========
        Paragraph summaryHeader = new Paragraph("Summary", headerFont);
        summaryHeader.setSpacingAfter(10f);
        document.add(summaryHeader);

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(60);
        summaryTable.setWidths(new float[]{5f, 3f});

        addCell(summaryTable, "Total Income", headerFont);
        addCell(summaryTable, responseDto.getTotalIncome().toString(), cellFont);

        addCell(summaryTable, "Total Expense", headerFont);
        addCell(summaryTable, responseDto.getTotalExpense().toString(), cellFont);

        addCell(summaryTable, "Amount available in selected range", headerFont);
        addCell(summaryTable, responseDto.getAmountAvailableTillNow().toString(), cellFont);

        document.add(summaryTable);

        // Add spacing before footer
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);

        // Closing paragraph
        Font closingFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11);
        Paragraph endDetails = new Paragraph("Team MoneyFi");
        Paragraph footerOne = new Paragraph("- This is a system-generated statement. Please don't reply to this mail.", closingFont);
        Paragraph footerTwo = new Paragraph("- Please contact moneyfi.owner@gmail.com for any issues.", closingFont);

        footerOne.setAlignment(Element.ALIGN_LEFT);
        footerTwo.setAlignment(Element.ALIGN_LEFT);

        document.add(endDetails);
        document.add(footerOne);
        document.add(footerTwo);

        document.close();
        return out.toByteArray();
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5f);
        table.addCell(cell);
    }
}
