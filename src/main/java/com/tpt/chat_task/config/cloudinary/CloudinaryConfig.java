package com.tpt.chat_task.config.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.tpt.chat_task.common.constant.AppConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                AppConstant.CLOUDINARY_CLOUD_NAME, cloudName,
                AppConstant.CLOUDINARY_API_KEY, apiKey,
                AppConstant.CLOUDINARY_API_SECRET, apiSecret
        ));
    }
}