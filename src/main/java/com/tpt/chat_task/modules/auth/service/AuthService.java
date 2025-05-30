package com.tpt.chat_task.modules.auth.service;

import com.tpt.chat_task.common.dto.SuccessResponse;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.auth.dto.request.LoginRequest;
import com.tpt.chat_task.modules.auth.dto.request.RegisterRequest;
import com.tpt.chat_task.modules.auth.dto.response.LoginResponse;

public interface AuthService {
    public LoginResponse login(LoginRequest loginRequest) throws NotFoundException;
    public String register(RegisterRequest registerRequest) throws NotFoundException;
}
