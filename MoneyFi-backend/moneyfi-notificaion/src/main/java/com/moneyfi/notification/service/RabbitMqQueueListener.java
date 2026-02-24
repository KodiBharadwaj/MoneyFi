package com.moneyfi.notification.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqQueueListener {

//    private final EmailService emailService;
//
//    public RabbitMqQueueListener(EmailService emailService) {
//        this.emailService = emailService;
//    }
//
//    @RabbitListener(queues = "${rabbitmq.queue}")
//    public void handlePasswordChanged(PasswordChangedEmailDto dto) {
//        emailService.sendPasswordChangedEmail(dto);
//    }
}