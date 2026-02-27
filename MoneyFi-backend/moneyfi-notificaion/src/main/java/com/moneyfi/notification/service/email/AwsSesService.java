package com.moneyfi.notification.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class AwsSesService {

    private final SesClient sesClient;

    public void sendEmailToUserUsingAwsSes(SimpleMailMessage simpleMailMessage) {
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
}
