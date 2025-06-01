package com.tpt.chat_task.modules.auth.service.impl;

import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.auth.constant.AuthError;
import com.tpt.chat_task.modules.auth.entity.Otp;
import com.tpt.chat_task.modules.auth.enums.OTP_STATUS;
import com.tpt.chat_task.modules.auth.repository.OtpRepository;
import com.tpt.chat_task.modules.auth.service.OtpService;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final OtpRepository otpRepository;

    @Override
    public boolean validateOtp(String otp) throws NotFoundException, BadRequestException {
        Otp findOtp = this.otpRepository.findByOtp(otp);
        if (findOtp == null) {
            throw new NotFoundException(AuthError.OTP_NOT_FOUND);
        }

        if(findOtp.getExpiredAt().isBefore(
                LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
        )) {
            throw new BadRequestException(AuthError.OTP_EXPIRED);
        }

        findOtp.setStatus(OTP_STATUS.VERIFIED);
        this.otpRepository.save(findOtp);

        return true;
    }

    @Override
    public String generateOtp(String userEmail) {
        Otp otp = new Otp();
        otp.setEmail(userEmail);
        otp.setStatus(OTP_STATUS.PENDING);
        otp.setExpiredAt(LocalDateTime.now().plusMinutes(5));
        otp.setOtp(UUID.randomUUID().toString());
        Otp saved = this.otpRepository.save(otp);
        return saved.getOtp();
    }
}
