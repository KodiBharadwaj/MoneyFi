package com.moneyfi.notification.service.email;

import com.moneyfi.notification.service.dto.emaildto.StatementAnalysisDto;
import com.moneyfi.notification.service.dto.emaildto.GmailSyncIncreaseRequestDto;
import com.moneyfi.notification.service.dto.emaildto.UserRaisedDefectDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    public void sendUserRaiseDefectEmailToAdmin(UserRaisedDefectDto userRaisedDefectDto){
        String subject = "MoneyFi's User Report Alert!!";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello Admin,</p>"
                + "<p style='font-size: 16px;'>You received the Report/defect by a user: </p>"
                + "<br>"
                + "<p style='font-size: 16px;'>" + userRaisedDefectDto.getMessage() + "</p>"
                + "<br> <hr>"
                + "<p style='font-size: 14px;'>" + userRaisedDefectDto.getName() + "</p>"
                + "<p style='font-size: 14px;'>" + userRaisedDefectDto.getEmail() + "</p>"
                + "<br>"
                + "<p style='font-size: 16px;'>Please login for more details</p>"
                + "</body>"
                + "</html>";
        MultipartFile image = userRaisedDefectDto.getImage();
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

    public void sendUserRaisedGmailSyncRequestEmailToAdmin(GmailSyncIncreaseRequestDto gmailSyncIncreaseRequestDto){
        String subject = "MoneyFi - User Gmail Sync Request Alert!!";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello Admin,</p>"
                + "<p style='font-size: 16px;'>You received the Gmail Sync Increase Count Request by a user </p>"
                + "<br>"
                + "<p style='font-size: 16px;'> Requested Count: " + gmailSyncIncreaseRequestDto.getCount() + "</p>"
                + "<p style='font-size: 16px;'> Requested Reason: " + gmailSyncIncreaseRequestDto.getReason() + "</p>"
                + "<br> <hr>"
                + "<p style='font-size: 14px;'>" + gmailSyncIncreaseRequestDto.getName() + "</p>"
                + "<p style='font-size: 14px;'>" + gmailSyncIncreaseRequestDto.getEmail() + "</p>"
                + "<br>"
                + "<p style='font-size: 16px;'>Please login for more details</p>"
                + "</body>"
                + "</html>";
        MultipartFile image = gmailSyncIncreaseRequestDto.getImage();
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

    public void sendAccountStatementAsEmail(StatementAnalysisDto statementAnalysisDto) {
        String name = statementAnalysisDto.getName();
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
        emailFilter.sendEmailWithAttachment(statementAnalysisDto.getUsername(), subject, body, statementAnalysisDto.getPdfByte(), fileNamePart+"_statement.pdf");
    }

    public void sendSpendingAnalysisEmail(StatementAnalysisDto statementAnalysisDto) {
        String name = statementAnalysisDto.getName();
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
        emailFilter.sendEmailWithAttachment(statementAnalysisDto.getUsername(), subject, body, statementAnalysisDto.getPdfByte(), fileNamePart + "_spending_analysis.pdf");
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
                + "<p style='font-size: 16px;'>Your Gmail Sync Count Increase Request has been approved by Admin and provided " + gmailSyncRequestCount + " chance/chances." + "</p>"
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

    public void sendUserGmailSyncCountIncreaseRequestRejection(String name, String email) {
        String subject = "Gmail Sync Request Rejected - MoneyFi";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + name + ",</p>"
                + "<p style='font-size: 16px;'>Your Gmail Sync Count Increase Request has been rejected by Admin." + "</p>"
                + "<p style='font-size: 16px;'>Please login to your account and find the remarks by Admin.</p>"
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

    public void sendNameChangeRequestApprovedMailToUser(String name, String email, String referenceNumber) {
        String subject = "Name Change Request Approved - MoneyFi";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + name + ",</p>"
                + "<p style='font-size: 16px;'>Your Name Change Request has been approved by Admin" + "</p>"
                + "<p style='font-size: 16px;'>Please login to your account and verify your name. Please use the reference number for tracking purpose: " + referenceNumber + "</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at " + ADMIN_EMAIL +"</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>Team MoneyFi</p>"
                + "</body>"
                + "</html>";
        emailFilter.sendEmail(email, subject, body);
    }

    public void sendNameChangeRequestRejectionMailToUser(String name, String email, String referenceNumber) {
        String subject = "Name Change Request Declined - MoneyFi";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + name + ",</p>"
                + "<p style='font-size: 16px;'>Your Name change Request has been rejected by Admin. Please use the reference number for tracking purpose: " +  referenceNumber + "</p>"
                + "<p style='font-size: 16px;'>Please login to your account and find the remarks by Admin.</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at " + ADMIN_EMAIL +"</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>Team MoneyFi</p>"
                + "</body>"
                + "</html>";
        emailFilter.sendEmail(email, subject, body);
    }

    public void sendAccountUnblockOrRetrievalSuccessfulMailToUser(String name, String email, String referenceNumber, String mode) {
        String subject = "Your Account " + mode +" - MoneyFi";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + name + ",</p>"
                + "<p style='font-size: 16px;'>Your account " + mode + "</p>"
                + "<p style='font-size: 16px;'>Please login to your account and start reusing the services. Please use the reference number for tracking purpose: " + referenceNumber + "</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at " + ADMIN_EMAIL +"</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>Team MoneyFi</p>"
                + "</body>"
                + "</html>";
        emailFilter.sendEmail(email, subject, body);
    }

    public void sendAccountUnblockOrRetrievalFailureMailToUser(String name, String email, String referenceNumber, String mode, String declineReason) {
        String subject = "Account " + mode + " Declined - MoneyFi";
        String body = "<html>"
                + "<body>"
                + "<p style='font-size: 16px;'>Hello " + name + ",</p>"
                + "<p style='font-size: 16px;'>Your Request has been rejected by Admin. Reason: " + declineReason + "</p>"
                + "<p style='font-size: 16px;'>Please use the reference number for tracking purpose: " +  referenceNumber + "</p>"
                + "<hr>"
                + "<p style='font-size: 14px; color: #555;'>If you have any issues, feel free to contact us at " + ADMIN_EMAIL +"</p>"
                + "<br>"
                + "<p style='font-size: 14px;'>Best regards,</p>"
                + "<p style='font-size: 14px;'>Team MoneyFi</p>"
                + "</body>"
                + "</html>";
        emailFilter.sendEmail(email, subject, body);
    }
}
