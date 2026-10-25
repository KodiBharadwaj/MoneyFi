package com.moneyfi.notification.service.email.impl;

import com.moneyfi.notification.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.communication.email-provider", havingValue = "aws_ses")
public class AwsSesService implements EmailService {

    @Value("${email.filter.from.email}")
    private String ADMIN_EMAIL;

    private final SesClient sesClient;

    @Override
    public void sendEmail(String email, String subject, String body) {
        SimpleMailMessage simpleMailMessage = functionToSetAwsSesObjectValues(email, subject, body);

        Destination destination = Destination.builder().toAddresses(simpleMailMessage.getTo()).build();
        Content subjectContent = Content.builder().data(simpleMailMessage.getSubject()).build();
        Content bodyContent = Content.builder().data(simpleMailMessage.getText()).build();
        Body messageBody = Body.builder().html(bodyContent).build();

        software.amazon.awssdk.services.ses.model.Message message =
                software.amazon.awssdk.services.ses.model.Message.builder()
                        .subject(subjectContent)
                        .body(messageBody)
                        .build();

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .source(simpleMailMessage.getFrom())  // Must be verified in SES sandbox
                .destination(destination)
                .message(message)
                .build();
        SendEmailResponse response = sesClient.sendEmail(emailRequest);
        log.info("Response from AWS SES: {}", response);
        System.out.println("Email sent successfully!");
    }

    @Override
    public void sendEmailWithAttachment(String email, String subject, String body, byte[] imageBytes, String fileName) {
        /**
         * Currently Sending Email with attachement using Sws ses is under development
         */
        sendEmail(email, subject, body);
    }

    private SimpleMailMessage functionToSetAwsSesObjectValues(String email, String subject, String body) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(ADMIN_EMAIL);
        simpleMailMessage.setTo(email);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(body);
        return simpleMailMessage;
    }
}
