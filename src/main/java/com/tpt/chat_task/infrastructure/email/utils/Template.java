package com.tpt.chat_task.infrastructure.email.utils;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;

public class Template {
    public static String getOtpHtmlTemplateAuth(String otp) throws IOException {
        // Read the HTML template file
        ClassPathResource resource = new ClassPathResource("templates/otp_template_auth.html");
        String template = Files.readString(resource.getFile().toPath());

        // Replace {{OTP}} with the actual OTP
        return template.replace("{{OTP}}", otp);
    }
}
