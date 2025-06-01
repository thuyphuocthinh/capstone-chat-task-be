package com.tpt.chat_task.infrastructure.email.service;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendEmailWithHtml(String to, String subject, String body) throws MessagingException;
    void sendEmailWithAttachment(String to, String subject, String body) throws MessagingException;
}