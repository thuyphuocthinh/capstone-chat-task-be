package com.tpt.chat_task.modules.user.service.impl;

import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.infrastructure.storage.service.UploadService;
import com.tpt.chat_task.modules.auth.constant.AuthError;
import com.tpt.chat_task.modules.auth.jwt.JwtProvider;
import com.tpt.chat_task.modules.user.dto.response.UserResponse;
import com.tpt.chat_task.modules.user.entity.User;
import com.tpt.chat_task.modules.user.enums.USER_ROLE;
import com.tpt.chat_task.modules.user.enums.USER_STATUS;
import com.tpt.chat_task.modules.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UploadService uploadService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testGetProfile_Success() {
        // given
        String token = "adfjlakdfa.asdfqwr.asdfasdf";
        String id = "asdf-asfd45-bxcvb-asdfa";

        User user = User.builder()
                .id("asdf-asfd45-bxcvb-asdfa")
                .email("test@gmail.com")
                .firstName("John")
                .lastName("Doe")
                .avatar("avatar.png")
                .status(USER_STATUS.ACTIVE)
                .role(USER_ROLE.MEMBER)
                .build();

        when(this.jwtProvider.getIdFromToken(token)).thenReturn(id);
        when(this.userRepository.findById(id)).thenReturn(
                Optional.ofNullable(user)
        );

        // then
        UserResponse response = userService.getProfile(token);

        // assert
        assertEquals(id, response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("MEMBER", response.getRole());
        assertEquals("avatar.png", response.getAvatar());
        assertEquals("ACTIVE", response.getStatus());
    }

    @Test
    void testGetProfile_Failure() {
        String token = "adfjlakdfa.asdfqwr.asdfasdf";
        String id = "asdf-asfd45-bxcvb-asdfa";

        when(this.jwtProvider.getIdFromToken(token)).thenReturn(id);
        when(this.userRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.getProfile(token);
        });

        assertEquals(AuthError.USER_NOT_FOUND, exception.getMessage());
    }
}