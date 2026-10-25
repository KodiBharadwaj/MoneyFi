package com.moneyfi.notification.service.email;

public interface EmailService {
    void sendEmail(String email, String subject, String body);

    void sendEmailWithAttachment(String adminEmail, String subject, String body, byte[] imageBytes, String fileName);
}
