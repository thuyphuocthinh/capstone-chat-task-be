package com.tpt.chat_task.infrastructure.email.utils;

import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class Template {
    public static String getOtpHtmlTemplateAuth(String otp) throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/otp_template_auth.html");

        // Đọc nội dung template từ classpath mà không dùng getFile()
        String template;
        try (InputStream is = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            template = reader.lines().collect(Collectors.joining("\n"));
        }

        // Thay {{OTP}} bằng giá trị thực
        return template.replace("{{OTP}}", otp);
    }
}
