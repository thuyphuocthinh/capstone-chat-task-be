package com.tpt.chat_task.modules.auth.service.impl;

import com.tpt.chat_task.modules.auth.constant.AuthError;
import com.tpt.chat_task.modules.auth.entity.CustomUserDetails;
import com.tpt.chat_task.modules.user.entity.User;
import com.tpt.chat_task.modules.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(AuthError.USER_NOT_FOUND));
        return new CustomUserDetails(user);
    }
}
