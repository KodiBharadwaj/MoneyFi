package com.moneyfi.user.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class EmailFilter {
    public static void sendEmail(String toEmail, String subject, String body) {
        String fromEmail = "bharadwajkodi2003@gmail.com";  // Sender's email
        String password = "pkiv sayk hirk kwvh";  // Sender's email password (Make sure it's correct or use App password)
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
            System.out.println("Email sent successfully!");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Failed to send email.");
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
