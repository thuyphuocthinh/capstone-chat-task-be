package com.tpt.chat_task.modules.auth.service;

import com.tpt.chat_task.common.exceptions.NotFoundException;
import org.apache.coyote.BadRequestException;

public interface OtpService {
    public boolean validateOtp(String otp) throws NotFoundException, BadRequestException;
    public String generateOtp(String email);
}
