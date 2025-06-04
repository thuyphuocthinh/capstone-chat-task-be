package com.tpt.chat_task.modules.user.service.impl;

import com.tpt.chat_task.common.constant.Metadata;
import com.tpt.chat_task.common.dto.SuccessResponseWithMetadata;
import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.infrastructure.storage.service.UploadService;
import com.tpt.chat_task.modules.auth.jwt.JwtProvider;
import com.tpt.chat_task.modules.user.constant.UserError;
import com.tpt.chat_task.modules.user.dto.request.ChangePasswordRequest;
import com.tpt.chat_task.modules.user.dto.request.ChangeRoleRequest;
import com.tpt.chat_task.modules.user.dto.request.UpdateAvatarRequest;
import com.tpt.chat_task.modules.user.dto.request.UpdateProfileRequest;
import com.tpt.chat_task.modules.user.dto.response.UserResponse;
import com.tpt.chat_task.modules.user.entity.User;
import com.tpt.chat_task.modules.user.enums.USER_STATUS;
import com.tpt.chat_task.modules.user.repository.UserRepository;
import com.tpt.chat_task.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final JwtProvider jwtProvider;

    private final PasswordEncoder passwordEncoder;

    private final UploadService uploadService;

    @Override
    public UserResponse getProfile(String token) throws NotFoundException {
        String userId = this.jwtProvider.getIdFromToken(token);
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        return UserResponse.builder()
                .id(userId)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().toString())
                .avatar(user.getAvatar())
                .status(user.getStatus().toString())
                .build();
    }

    @Override
    public UserResponse getUserById(String id) throws NotFoundException {
        User user = this.userRepository.findById(id).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        return UserResponse.builder()
                .id(id)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().toString())
                .status(user.getStatus().toString())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public SuccessResponseWithMetadata<?> getListUsers(Integer page, Integer paging) throws NotFoundException {
        Pageable pageable =  PageRequest.of(Math.max(0, page - 1), paging);
        Page<User> userPage = this.userRepository.findAllByStatus(USER_STATUS.ACTIVE, pageable);
        List<User> users = userPage.getContent();
        List<UserResponse> userResponses = users.stream().map(user ->
            UserResponse.builder()
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .email(user.getEmail())
                    .lastName(user.getLastName())
                    .status(user.getStatus().toString())
                    .role(user.getRole().toString())
                    .avatar(user.getAvatar())
                    .build()
        ).toList();

        Metadata metadata = Metadata.builder()
                .currentPage(userPage.getNumber() + 1)
                .totalPages(userPage.getTotalPages())
                .totalElements((int) userPage.getTotalElements())
                .pageSize(userPage.getSize())
                .build();

        return SuccessResponseWithMetadata.builder()
                .metadata(metadata)
                .data(userResponses)
                .build();
    }

    @Override
    public UserResponse updateProfile(String token, UpdateProfileRequest request) throws NotFoundException {
        String id = this.jwtProvider.getIdFromToken(token);
        User user = this.userRepository.findById(id).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user = this.userRepository.save(user);
        return UserResponse.builder()
                .id(id)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().toString())
                .avatar(user.getAvatar())
                .status(user.getStatus().toString())
                .build();
    }

    @Override
    public UserResponse changePassword(String token, ChangePasswordRequest request) throws NotFoundException, BadRequestException {
        String id = this.jwtProvider.getIdFromToken(token);
        User user = this.userRepository.findById(id).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        String currentPassword = request.getCurrentPassword();
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();

        if(!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException(UserError.CURRENT_PASSWORD_WRONG);
        }

        if(!newPassword.equals(confirmPassword)) {
            throw new BadRequestException(UserError.PASSWORD_MISMATCH);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user = this.userRepository.save(user);

        return UserResponse.builder()
                .id(id)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().toString())
                .avatar(user.getAvatar())
                .status(user.getStatus().toString())
                .build();
    }

    @Override
    public String deleteUserById(String id) throws NotFoundException {
        User user = this.userRepository.findById(id).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        this.userRepository.delete(user);
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public UserResponse changeRole(String id, ChangeRoleRequest request) throws NotFoundException {
        User user = this.userRepository.findById(id).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        user.setRole(request.getRole());
        user = this.userRepository.save(user);
        return UserResponse.builder()
                .id(id)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().toString())
                .avatar(user.getAvatar())
                .status(user.getStatus().toString())
                .build();
    }

    @Override
    public UserResponse updateAvatar(String token, UpdateAvatarRequest request) throws NotFoundException {
        String id = this.jwtProvider.getIdFromToken(token);
        User user = this.userRepository.findById(id).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        Map<String, Object> uploadResult = null;
        try {
            uploadResult = this.uploadService.uploadOneFile(request.getAvatar());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        String imageUrl = (String) uploadResult.getOrDefault("secure_url", "");
        user.setAvatar(imageUrl);
        user = this.userRepository.save(user);
        return UserResponse.builder()
                .id(id)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().toString())
                .avatar(user.getAvatar())
                .status(user.getStatus().toString())
                .build();
    }
}
