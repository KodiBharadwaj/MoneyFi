package com.moneyfi.notification.service.email;

import com.moneyfi.notification.exceptions.CustomInternalServerErrorException;
import com.moneyfi.notification.util.constants.StringConstants;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@Slf4j
public class EmailFilter {

    @Value("${email.filter.from.email}")
    private String fromEmail;
    @Value("${email.filter.from.password}")
    private String password;

    private static final String EMAIL_SENT_FAILURE_MESSAGE = "Can't send email, server error occurred";

    public boolean sendEmail(String toEmail, String subject, String body) {
        String host = "smtp.gmail.com";  // Gmail SMTP server
        String port = "587";  // SMTP port for Gmail
        // Set up properties for the SMTP server
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        // Enable STARTTLS (Port 587), fallback to SSL (Port 465)
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.ssl.enable", "false");
        // Fallback to SSL if STARTTLS fails (for port 465)
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", "true");
        // Get the Session object for authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });
        try {
            // Create the email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            // Set the email content to HTML type
            message.setContent(body, "text/html; charset=UTF-8");  // Change to HTML content
            // Send the email
            Transport.send(message);
            log.info(StringConstants.EMAIL_SENT_SUCCESS_MESSAGE);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            log.info("Failed to send mail ", e);
            throw new CustomInternalServerErrorException(EMAIL_SENT_FAILURE_MESSAGE);
        }
    }

    public boolean sendEmailWithAttachment(String toEmail, String subject, String body, byte[] attachmentBytes, String fileName) {
        String host = "smtp.gmail.com";
        String port = "587";

        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.ssl.enable", "false");
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            // Create the email body part
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(body, "text/html; charset=UTF-8");
            // Create the PDF attachment part
            MimeBodyPart attachmentPart = new MimeBodyPart();
            DataSource dataSource = new ByteArrayDataSource(attachmentBytes, "application/pdf");
            attachmentPart.setDataHandler(new DataHandler(dataSource));
            attachmentPart.setFileName(fileName);
            // Combine parts into a multipart
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentPart);
            // Set the complete message parts
            message.setContent(multipart);
            // Send email
            Transport.send(message);
            log.info(StringConstants.EMAIL_SENT_SUCCESS_MESSAGE);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            log.error("Failed to send email with attachment", e);
            return false;
        }
    }

}
