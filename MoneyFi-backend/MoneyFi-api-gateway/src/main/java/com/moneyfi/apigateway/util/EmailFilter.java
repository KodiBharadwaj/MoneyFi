package com.moneyfi.apigateway.util;

import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.Random;

@Component
@Slf4j
public class EmailFilter {
    private EmailFilter(){}

    public static boolean sendEmail(String toEmail, String subject, String body) {
        String fromEmail = "bharadwajkodi2003@gmail.com";  // Sender's email
        String password = "xxxx xxxx xxxx xxxx";  // Sender's email password (Make sure it's correct or use App password)
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
            log.info("Email sent successfully!");
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            log.info("Failed to send mail ", e);
            return false;
        }
    }

    public static String generateVerificationCode() {
        Random random = new Random();
        int verificationCode = 100000 + random.nextInt(900000);
        return String.valueOf(verificationCode);
    }
}
