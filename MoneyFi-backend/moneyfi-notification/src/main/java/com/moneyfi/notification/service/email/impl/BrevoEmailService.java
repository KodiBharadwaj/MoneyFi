package com.moneyfi.notification.service.email.impl;

import com.moneyfi.notification.service.email.EmailService;
import com.moneyfi.notification.util.constants.StringConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.communication.email-provider", havingValue = "brevo")
public class BrevoEmailService implements EmailService {

    @Value("${email.brevo.api-key}")
    private String apiKey;

    @Value("${email.brevo.from-email}")
    private String fromEmail;

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    public void sendEmail(String toEmail, String subject, String body) {

        log.info("Brevo API Key present: {}", apiKey != null);
        log.info("Brevo API Key length: {}", apiKey != null ? apiKey.length() : 0);
        log.info("From Email: {}", fromEmail);


        try {
            URL url = new URL(BREVO_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("accept", "application/json");
            conn.setRequestProperty("api-key", apiKey);
            conn.setRequestProperty("content-type", "application/json");
            conn.setDoOutput(true);

            String payload = "{"
                    + "\"sender\":{\"email\":\"" + fromEmail + "\"},"
                    + "\"to\":[{\"email\":\"" + toEmail + "\"}],"
                    + "\"subject\":\"" + subject + "\","
                    + "\"htmlContent\":\"" + body + "\""
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            log.info("Brevo response code: {}", responseCode);

            log.info(StringConstants.EMAIL_SENT_SUCCESS_MESSAGE + " with Brevo mail service");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Brevo email sending failed", e);
        }
    }

    public void sendEmailWithAttachment(String toEmail, String subject, String body, byte[] attachmentBytes, String fileName) {
        try {
            URL url = new URL(BREVO_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("accept", "application/json");
            conn.setRequestProperty("api-key", apiKey);
            conn.setRequestProperty("content-type", "application/json");
            conn.setDoOutput(true);

            String encodedFile = Base64.getEncoder().encodeToString(attachmentBytes);

            String payload = "{"
                    + "\"sender\":{\"email\":\"" + fromEmail + "\"},"
                    + "\"to\":[{\"email\":\"" + toEmail + "\"}],"
                    + "\"subject\":\"" + subject + "\","
                    + "\"htmlContent\":\"" + body + "\","
                    + "\"attachment\":[{"
                    + "\"content\":\"" + encodedFile + "\","
                    + "\"name\":\"" + fileName + "\""
                    + "}]"
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            log.info(StringConstants.EMAIL_SENT_SUCCESS_MESSAGE + " with Brevo mail service");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Brevo attachment email failed", e);
        }
    }
}