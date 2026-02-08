package com.moneyfi.user.util;

import com.moneyfi.user.service.common.dto.request.GmailSyncCountIncreaseRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class EmailTemplates {

    @Value("${email.filter.from.email}")
    private String ADMIN_EMAIL;

    private final EmailFilter emailFilter;

    public EmailTemplates(@Autowired(required = false) EmailFilter emailFilter){
        this.emailFilter = emailFilter;
    }

    public void sendPasswordChangeAlertMail(String userName, String email){
        String subject = "Password Change Alert!!";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + userName +",</p>"
                + "<p style='font-size: 16px;'>You have changed the password for your account with username: " + email + "</p>"
                + "<p style='font-size: 20px; font-weight: bold; color: #007BFF;'> </p>"
                + "<p style='font-size: 16px;'>Kindly Ignore if it by you. If not, reply to this mail immediately to secure account.</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at " + ADMIN_EMAIL +"</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>Team MoneyFi</p>"
                + "</body>"
                + "</html>";
        emailFilter.sendEmail(email, subject, body);
    }

    public void sendUserReportStatusMailToUser(String name, String referenceNumber, String description, String email){
        String subject = "Report Raised Status";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + name +",</p>"
                + "<p style='font-size: 16px;'>Please find the status of your request: " + description + "</p>"
                + "<br>"
                + "<p style='font-size: 16px;'>For more details, track your request in moneyfi with reference number: " + referenceNumber + "</p>"
                + "<p style='font-size: 20px; font-weight: bold; color: #007BFF;'> </p>"
                + "<p style='font-size: 16px;'>Kindly Ignore if it by you. If not, reply to this mail immediately to secure account.</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at " + ADMIN_EMAIL +"</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>Team MoneyFi</p>"
                + "</body>"
                + "</html>";
        emailFilter.sendEmail(email, subject, body);
    }

    public boolean sendOtpEmailToUserForSignup(String email, String name, String verificationCode){
        String subject = "OTP for MoneyFi's account creation";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + name + ",</p>"
                + "<p style='font-size: 16px;'>You have requested for account creation. Please use the following verification code:</p>"
                + "<p style='font-size: 20px; font-weight: bold; color: #007BFF;'>" + verificationCode + "</p>"
                + "<p style='font-size: 16px;'>This code is valid for 5 minutes only. If you did not raise, please ignore this email.</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at " + ADMIN_EMAIL +"</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>Team MoneyFi</p>"
                + "</body>"
                + "</html>";
        return emailFilter.sendEmail(email, subject, body);
    }

    public void sendOtpToUserForAccountBlock(String username, String name, String verificationCode, String type){
        String message = null;
        if (type.equalsIgnoreCase("BLOCK")) {
            message = "Block";
        } else if (type.equalsIgnoreCase("DELETE")) {
            message = "Delete";
        }
        String subject = "OTP to " + message + " account";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + name + ",</p>"
                + "<p style='font-size: 16px;'>You have requested otp for account " + message + ". Please use the following verification code:</p>"
                + "<p style='font-size: 20px; font-weight: bold; color: #007BFF;'>" + verificationCode + "</p>"
                + "<p style='font-size: 16px;'>This code is valid for 5 minutes only. If you did not raise, please ignore this email.</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at " + ADMIN_EMAIL +"</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>Team MoneyFi</p>"
                + "</body>"
                + "</html>";
        emailFilter.sendEmail(username, subject, body);
    }

    public void sendUserNameToUser(String username){
        String subject = "MoneyFi - Username request";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello User,</p>"
                + "<p style='font-size: 16px;'>You have requested for username with your details. Here is you username: " + username + "</p>"
                + "<p style='font-size: 20px; font-weight: bold; color: #007BFF;'> </p>"
                + "<p style='font-size: 16px;'>Kindly Ignore if it by you. If not, reply to this mail immediately to secure account.</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at " + ADMIN_EMAIL +"</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>Team MoneyFi</p>"
                + "</body>"
                + "</html>";
        emailFilter.sendEmail(username, subject, body);
    }

    public void sendOtpForForgotPassword(String userName, String email, String verificationCode){
        String subject = "MoneyFi's Password Reset Verification Code";
        String body = "<html>"
                + "<body>"
                + "<h2 style='color: #333;'>Password Reset Verification</h2>"
                + "<p style='font-size: 16px;'>Hello " + userName + ",</p>"
                + "<p style='font-size: 16px;'>You have requested to reset your password. Please use the following verification code:</p>"
                + "<p style='font-size: 20px; font-weight: bold; color: #007BFF;'>" + verificationCode + "</p>"
                + "<p style='font-size: 16px;'>This code is valid for 5 minutes only. If you did not raise, please ignore this email.</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at " + ADMIN_EMAIL +"</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>Team MoneyFi</p>"
                + "</body>"
                + "</html>";
        emailFilter.sendEmail(email, subject, body);
    }

    public void sendAnniversaryCongratulationsMailToUser(String email, String name, int numberOfYears){
        String subject = "Happy Anniversary - MoneyFi";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + name + ",</p>"
                + "<p style='font-size: 16px;'>We wish you a very happy anniversary in our moneyfi. May god bless you on this auspicious day.</p>"
                + "<p style='font-size: 16px;'>You have completed " + numberOfYears + (numberOfYears == 1?" year" : " years") + " in our platform. Hope you are enjoying the services.</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at " + ADMIN_EMAIL +"</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>Team MoneyFi</p>"
                + "</body>"
                + "</html>";
        emailFilter.sendEmail(email, subject, body);
    }

    public void sendBirthdayWishEmailToUsers(String email, String name){
        String subject = "Happy Birthday";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + name + ",</p>"
                + "<p style='font-size: 16px;'>We wish you a very happy birthday to you. May god bless you on this auspicious day.</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at " + ADMIN_EMAIL +"</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>Team MoneyFi</p>"
                + "</body>"
                + "</html>";
        emailFilter.sendEmail(email, subject, body);
    }

    public void sendUserRaiseDefectEmailToAdmin(com.moneyfi.user.service.common.dto.request.UserDefectRequestDto userDefectRequestDto, MultipartFile image){
        String subject = "MoneyFi's User Report Alert!!";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello Admin,</p>"
                + "<p style='font-size: 16px;'>You received the Report/defect by a user: </p>"
                + "<br>"
                + "<p style='font-size: 16px;'>" + userDefectRequestDto.getMessage() + "</p>"
                + "<br> <hr>"
                + "<p style='font-size: 14px;'>" + userDefectRequestDto.getName() + "</p>"
                + "<p style='font-size: 14px;'>" + userDefectRequestDto.getEmail() + "</p>"
                + "<br>"
                + "<p style='font-size: 16px;'>Please login for more details</p>"
                + "</body>"
                + "</html>";
        if (image != null && !image.isEmpty()) {
            try {
                byte[] imageBytes = image.getBytes();
                emailFilter.sendEmailWithAttachment(ADMIN_EMAIL, subject, body, imageBytes, image.getOriginalFilename() != null ? image.getOriginalFilename() : "attachment.jpg");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            emailFilter.sendEmail(ADMIN_EMAIL, subject, body);
        }
    }

    public void sendUserRaisedGmailSyncRequestEmailToAdmin(GmailSyncCountIncreaseRequestDto request, String name, String email, MultipartFile image){
        String subject = "MoneyFi - User Gmail Sync Request Alert!!";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello Admin,</p>"
                + "<p style='font-size: 16px;'>You received the Gmail Sync Increase Count Request by a user: </p>"
                + "<br>"
                + "<p style='font-size: 16px;'> Request Reason: " + request.getReason() + "</p>"
                + "<br> <hr>"
                + "<p style='font-size: 14px;'>" + name + "</p>"
                + "<p style='font-size: 14px;'>" + email + "</p>"
                + "<br>"
                + "<p style='font-size: 16px;'>Please login for more details</p>"
                + "</body>"
                + "</html>";
        if (image != null && !image.isEmpty()) {
            try {
                byte[] imageBytes = image.getBytes();
                emailFilter.sendEmailWithAttachment(ADMIN_EMAIL, subject, body, imageBytes, image.getOriginalFilename() != null ? image.getOriginalFilename() : "attachment.jpg");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            emailFilter.sendEmail(ADMIN_EMAIL, subject, body);
        }
    }

    public void sendUserFeedbackEmailToAdmin(String rating, String message){
        String subject = "MoneyFi's User Feedback";

        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello Admin,</p>"
                + "<br>"
                + "<p style='font-size: 16px;'> You received feedback: " + rating + "/5 </p>"
                + "<br>"
                + "<p style='font-size: 16px;'>Comment: </p>"
                + "<p style='font-size: 16px;'>" + message + "</p>";
        emailFilter.sendEmail(ADMIN_EMAIL, subject, body);
    }

    public boolean sendAccountStatementAsEmail(String name, String username, byte[] pdfBytes) {
        String subject = "MoneyFi - Account Statement";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + name + ", </p>"
                + "<p style='font-size: 16px;'>You have requested for account statement. Kindly find the attached PDF.</p>"
                + "<p style='font-size: 16px;'>The format for the password is: </p>"
                + "<p style='font-size: 16px;'>First four characters in your name in capital + First four characters in username in small letters </p> <br>"
                + "<p style='font-size: 16px;'>Lets say the name and username are: abcxyz, sample123@gmail.com. Then the password will be <strong> ABCXsamp </strong> </p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at " + ADMIN_EMAIL + "</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>Team MoneyFi</p>"
                + "</body>"
                + "</html>";
        String fileNamePart;
        int spaceIndex = name.indexOf(' ');
        if (spaceIndex > 0) {
            fileNamePart = name.substring(0, spaceIndex);
        } else {
            fileNamePart = name;
        }
        return emailFilter.sendEmailWithAttachment(username, subject, body, pdfBytes, fileNamePart+"_statement.pdf");
    }

    public boolean sendSpendingAnalysisEmail(String name, String username, byte[] pdfBytes) {
        String subject = "MoneyFi - Spending Analysis Report";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + name + ", </p>"
                + "<p style='font-size: 16px;'>You have requested for spending analysis report. Kindly find the attached PDF.</p>"
                + "<p style='font-size: 16px;'>The format for the password is: </p>"
                + "<p style='font-size: 16px;'>First four characters in your name in capital + First four characters in username in small letters </p> <br>"
                + "<p style='font-size: 16px;'>Lets say the name and username are: abcxyz, sample123@gmail.com. Then the password will be <strong> ABCXsamp </strong> </p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at " + ADMIN_EMAIL + "</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>Team MoneyFi</p>"
                + "</body>"
                + "</html>";
        String fileNamePart;
        int spaceIndex = name.indexOf(' ');
        if (spaceIndex > 0) {
            fileNamePart = name.substring(0, spaceIndex);
        } else {
            fileNamePart = name;
        }
        return emailFilter.sendEmailWithAttachment(username, subject, body, pdfBytes, fileNamePart + "_spending_analysis.pdf");
    }

    public void sendReferenceNumberEmailToUser(String name, String email, String description, String referenceNumber) {
        String subject = "MoneyFi - user requests";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + name + "</p>"
                + "<p style='font-size: 16px;'>You have requested for reference number to " + description + ". Here is you reference number: " + referenceNumber + "</p>"
                + "<p style='font-size: 20px; font-weight: bold; color: #007BFF;'> </p>"
                + "<p style='font-size: 16px;'>Please save your reference number to track the status of your request.</p>"
                + "<p style='font-size: 20px; font-weight: bold; color: #007BFF;'> </p>"
                + "<p style='font-size: 16px;'>Kindly Ignore if it by you. If not, reply to this mail immediately to secure account.</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at " + ADMIN_EMAIL +"</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>Team MoneyFi</p>"
                + "</body>"
                + "</html>";
        emailFilter.sendEmail(email, subject, body);
    }

    public SimpleMailMessage sendEmailForSuccessfulUserCreation(String name, String email){
        String subject = "Welcome to MoneyFi";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + name +",</p>"
                + "<p style='font-size: 16px;'>You have successfully created account in MoneyFi. Kindly Login to use our services. </p>"
                + "<p style='font-size: 20px; font-weight: bold; color: #007BFF;'> </p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at " + ADMIN_EMAIL +"</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>Team MoneyFi</p>"
                + "</body>"
                + "</html>";
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(ADMIN_EMAIL);
        simpleMailMessage.setTo(email);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(body);
        return simpleMailMessage;
    }

    public void sendBlockAlertMailToUser(String email, String reason, String name, byte[] file) {
        String subject = "Account Block Alert!!";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + name + ",</p>"
                + "<p style='font-size: 16px;'>Your account has been blocked by admin due to the reason: " + reason + "</p>"
                + "<p style='font-size: 16px;'>Please contact admin for more details or raise unblock request</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at " + ADMIN_EMAIL +"</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>Team MoneyFi</p>"
                + "</body>"
                + "</html>";
        emailFilter.sendEmailWithAttachment(email, subject, body, file, "reason-attachment.pdf");
    }

    public void sendUserGmailSyncApprovedMail(String name, String email, int gmailSyncRequestCount) {
        String subject = "Gmail Sync Request Approved - MoneyFi";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + name + ",</p>"
                + "<p style='font-size: 16px;'>Your Gmail Sync Count Increase Request has been approved by Admin and provided " + gmailSyncRequestCount + " chances." + "</p>"
                + "<p style='font-size: 16px;'>Please login to your account and use the services.</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at " + ADMIN_EMAIL +"</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>Team MoneyFi</p>"
                + "</body>"
                + "</html>";
        emailFilter.sendEmail(email, subject, body);
    }

    public void sendContactUsDetailsEmailToAdmin(String email, String phoneNumber, String name, String description) {
        String subject = "MoneyFi - Help center/Contact";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello admin, </p>"
                + "<p style='font-size: 16px;'>You got a mail through MoneyFi with contact details: </p>"
                + "<br>"
                + "<p style='font-size: 16px;'>" + name + "</p>"
                + "<p style='font-size: 16px;'>" + email + "</p>"
                + "<p style='font-size: 16px;'>" + phoneNumber + "</p>"
                + "<p style='font-size: 16px;'> Description: " + description + "</p>"
                + "<p style='font-size: 20px; font-weight: bold; color: #007BFF;'> </p>"
                + "<hr>"
                + "<br>"
                + "</body>"
                + "</html>";
        emailFilter.sendEmail(ADMIN_EMAIL, subject, body);
    }
}
