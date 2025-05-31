package com.tpt.chat_task.infrastructure.email.service.impl;

import com.tpt.chat_task.infrastructure.email.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class EmailServiceImpl implements EmailService {
    @Value("${spring.mail.username}")
    private String senderEmail;

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendEmailWithHtml(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipients(MimeMessage.RecipientType.TO, to);
        message.setSubject(subject);
        message.setContent(body, "text/html; charset=utf-8");

        mailSender.send(message);
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String body) throws MessagingException {

    }
}
