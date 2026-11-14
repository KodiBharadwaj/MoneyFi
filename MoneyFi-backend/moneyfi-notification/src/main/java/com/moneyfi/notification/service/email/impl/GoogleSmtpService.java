package com.moneyfi.notification.service.email.impl;

import com.moneyfi.notification.exceptions.CustomInternalServerErrorException;
import com.moneyfi.notification.service.email.EmailService;
import com.moneyfi.notification.util.constants.StringConstants;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.communication.email-provider", havingValue = "google_smtp", matchIfMissing = true)
public class GoogleSmtpService implements EmailService {

    @Value("${email.filter.from.email}")
    private String fromEmail;

    @Value("${email.filter.from.password}")
    private String password;

    public void sendEmail(String toEmail, String subject, String body) {
        Properties properties = getSmtpProperties();
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
            message.setContent(body, "text/html; charset=UTF-8");
            Transport.send(message);
            log.info(StringConstants.EMAIL_SENT_SUCCESS_MESSAGE + " with Java SMTP");
        } catch (MessagingException e) {
            log.error("Failed to send mail ", e);
            throw new CustomInternalServerErrorException(e.getMessage());
        }
    }

    public void sendEmailWithAttachment(String toEmail, String subject, String body, byte[] attachmentBytes, String fileName) {
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
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(body, "text/html; charset=UTF-8");
            MimeBodyPart attachmentPart = new MimeBodyPart();
            DataSource dataSource = new ByteArrayDataSource(attachmentBytes, "application/pdf");
            attachmentPart.setDataHandler(new DataHandler(dataSource));
            attachmentPart.setFileName(fileName);
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentPart);
            message.setContent(multipart);
            Transport.send(message);
            log.info(StringConstants.EMAIL_SENT_SUCCESS_MESSAGE + " with Java SMTP");
        } catch (MessagingException e) {
            log.error("Failed to send mail ", e);
            throw new CustomInternalServerErrorException(e.getMessage());
        }
    }

    private static Properties getSmtpProperties() {
        String host = "smtp.gmail.com";
        String port = "587";
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.ssl.enable", "false");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", "true");
        return properties;
    }
}
