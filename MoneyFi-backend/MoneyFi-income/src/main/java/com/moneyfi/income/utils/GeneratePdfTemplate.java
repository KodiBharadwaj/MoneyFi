package com.moneyfi.income.utils;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.moneyfi.income.service.dto.response.AccountStatementResponseDto;
import com.moneyfi.income.service.dto.response.UserDetailsForStatementDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

@Component
public class GeneratePdfTemplate {

    @Value("${account.statement.admin.pdf.password}")
    private String adminPassword;

    public byte[] generatePdf(List<AccountStatementResponseDto> transactions, UserDetailsForStatementDto userDetails, LocalDate fromDate, LocalDate toDate, String userPassword) {
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

        // Add title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Account Statement", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
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

        // Table header
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 3f, 7f, 3f, 3f});

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        addCell(table, "Date", headerFont);
        addCell(table, "Time", headerFont);
        addCell(table, "Description", headerFont);
        addCell(table, "Amount Credited", headerFont);
        addCell(table, "Amount Debited", headerFont);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // or "dd MMM yyyy"

        // Table data
        for (AccountStatementResponseDto transaction : transactions) {
            String formattedDate = sdf.format(transaction.getTransactionDate());
            addCell(table, formattedDate);
            addCell(table, transaction.getTransactionTime());
            addCell(table, transaction.getDescription());
            if(transaction.getCreditOrDebit().equalsIgnoreCase(CreditOrDebit.CREDIT.name())){
                addCell(table, transaction.getAmount().toString());
                addCell(table, "-");
            } else if (transaction.getCreditOrDebit().equalsIgnoreCase(CreditOrDebit.DEBIT.name())){
                addCell(table, "-");
                addCell(table, transaction.getAmount().toString());
            }
        }

        document.add(table);

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
    private void addCell(PdfPTable table, String text) {
        table.addCell(new PdfPCell(new Phrase(text)));
    }

    private void addCell(PdfPTable table, String text, Font font) {
        table.addCell(new PdfPCell(new Phrase(text, font)));
    }
}
