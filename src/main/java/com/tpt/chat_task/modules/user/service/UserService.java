package com.tpt.chat_task.modules.user.service;

import com.tpt.chat_task.common.dto.SuccessResponseWithMetadata;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.user.dto.request.ChangePasswordRequest;
import com.tpt.chat_task.modules.user.dto.request.ChangeRoleRequest;
import com.tpt.chat_task.modules.user.dto.request.UpdateAvatarRequest;
import com.tpt.chat_task.modules.user.dto.request.UpdateProfileRequest;
import com.tpt.chat_task.modules.user.dto.response.UserResponse;
import com.tpt.chat_task.modules.user.entity.User;
import org.apache.coyote.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    public UserResponse getProfile(String token) throws NotFoundException;
    public UserResponse getUserById(String id) throws NotFoundException;
    public SuccessResponseWithMetadata<?> getListUsers(Integer page, Integer paging) throws NotFoundException;
    public UserResponse updateProfile(String token, UpdateProfileRequest request) throws NotFoundException;
    public UserResponse changePassword(String token, ChangePasswordRequest request) throws NotFoundException, BadRequestException;
    public String deleteUserById(String id) throws NotFoundException;
    public UserResponse changeRole(String id, ChangeRoleRequest request) throws NotFoundException;
    public UserResponse updateAvatar(String token, UpdateAvatarRequest request) throws NotFoundException;
    // update avatar
}
