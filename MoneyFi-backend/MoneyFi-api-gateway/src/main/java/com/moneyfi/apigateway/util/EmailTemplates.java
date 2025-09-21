package com.moneyfi.apigateway.util;

import com.moneyfi.apigateway.service.common.dto.request.UserDefectRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.util.Base64;

import static com.moneyfi.apigateway.util.constants.StringUtils.ADMIN_EMAIL;

@Component
public class EmailTemplates {

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

    public boolean sendOtpToUserForAccountBlock(String username, String name, String verificationCode){
        String subject = "OTP to block account";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + name + ",</p>"
                + "<p style='font-size: 16px;'>You have requested otp for account block. Please use the following verification code:</p>"
                + "<p style='font-size: 20px; font-weight: bold; color: #007BFF;'>" + verificationCode + "</p>"
                + "<p style='font-size: 16px;'>This code is valid for 5 minutes only. If you did not raise, please ignore this email.</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at " + ADMIN_EMAIL +"</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>Team MoneyFi</p>"
                + "</body>"
                + "</html>";
        return emailFilter.sendEmail(username, subject, body);
    }

    public boolean sendUserNameToUser(String username){
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
        return emailFilter.sendEmail(username, subject, body);
    }

    public boolean sendOtpForForgotPassword(String userName, String email, String verificationCode){
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
        return emailFilter.sendEmail(email, subject, body);
    }

    public void sendBirthdayMail(String email, String name, int numberOfYears){
        String subject = "Happy Birthday";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + name + ",</p>"
                + "<p style='font-size: 16px;'>We wish you a very happy birthday. May god bless you on this auspicious day.</p>"
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

    public void sendUserRaiseDefectEmailToAdmin(UserDefectRequestDto userDefectRequestDto, String images){
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

        if (images.contains(",")) {
            images = images.split(",")[1];  // Only the base64 part after the comma
        }
        byte[] imageBytes = Base64.getDecoder().decode(images);
        emailFilter.sendEmailWithAttachment(ADMIN_EMAIL, subject, body, imageBytes, "user.jpg");
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
        int index = name.indexOf(' ');
        String fileName = name.substring(0, index)+"_statement.pdf";
        return emailFilter.sendEmailWithAttachment(username, subject, body, pdfBytes, fileName);
    }

    public boolean sendReferenceNumberEmail(String name, String email, String description, String referenceNumber) {
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
        return emailFilter.sendEmail(email, subject, body);
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
}
