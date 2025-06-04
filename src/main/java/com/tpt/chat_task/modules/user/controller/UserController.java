package com.tpt.chat_task.modules.user.controller;

import com.tpt.chat_task.common.constant.AppConstant;
import com.tpt.chat_task.common.constant.JwtConstant;
import com.tpt.chat_task.common.dto.SuccessResponse;
import com.tpt.chat_task.modules.user.dto.request.ChangePasswordRequest;
import com.tpt.chat_task.modules.user.dto.request.ChangeRoleRequest;
import com.tpt.chat_task.modules.user.dto.request.UpdateProfileRequest;
import com.tpt.chat_task.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken
    ) {
        String accessToken = bearerToken.substring(7);
        SuccessResponse successResponse = SuccessResponse.builder()
                .data(this.userService.getProfile(accessToken))
                .build();
        return ResponseEntity.ok(successResponse);
    }

    @GetMapping("")
    public ResponseEntity<?> getListUsers(
            @RequestParam(name = "page", required = false, defaultValue = AppConstant.PAGE) Integer page,
            @RequestParam(name = "paging", required = false, defaultValue = AppConstant.PAGING) Integer paging
    ) {
        return ResponseEntity.ok(this.userService.getListUsers(page, paging));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable String id
    ) {
        SuccessResponse successResponse = SuccessResponse.builder()
                .data(this.userService.getUserById(id))
                .build();
        return ResponseEntity.ok(successResponse);
    }

    @PatchMapping("/{id}/change-role")
    public ResponseEntity<?> getUserById(
            @PathVariable String id,
            @RequestBody @Valid ChangeRoleRequest request
    ) {
        SuccessResponse successResponse = SuccessResponse.builder()
                .data(this.userService.changeRole(id, request))
                .build();
        return ResponseEntity.ok(successResponse);
    }

    @PatchMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestBody @Valid UpdateProfileRequest request,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken
    ) {
        String accessToken = bearerToken.substring(7);
        SuccessResponse successResponse = SuccessResponse.builder()
                .data(this.userService.updateProfile(accessToken, request))
                .build();
        return ResponseEntity.ok(successResponse);
    }

    @PatchMapping("/profile/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody @Valid ChangePasswordRequest request,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken
    ) throws BadRequestException {
        String accessToken = bearerToken.substring(7);
        SuccessResponse successResponse = SuccessResponse.builder()
                .data(this.userService.changePassword(accessToken, request))
                .build();
        return ResponseEntity.ok(successResponse);
    }
}
