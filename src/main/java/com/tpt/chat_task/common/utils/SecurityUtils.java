package com.tpt.chat_task.common.utils;

import com.tpt.chat_task.common.constant.ErrorConstant;
import com.tpt.chat_task.modules.auth.entity.CustomUserDetails;
import com.tpt.chat_task.modules.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class SecurityUtils {
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Current authentication: {}", authentication);
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUser().getId().toString();
        }
        throw new RuntimeException(ErrorConstant.UNAUTHORIZED);
    }

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUser();
        }
        throw new RuntimeException(ErrorConstant.UNAUTHORIZED);
    }
}