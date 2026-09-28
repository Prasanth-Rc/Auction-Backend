package com.auction.service;

import com.auction.entity.User;
import com.auction.repository.UserRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        User u = userRepository.findByEmailOrMobileNative(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getPassword())
                .authorities(u.getRoles().toArray(String[]::new))
                .build();
    }

    @Builder
    public record AuthResponse(Long userId, String fullName, String email, String mobile,
                               String companyName, String representing, boolean newUser) {}

    @Transactional(readOnly = true)
    public AuthResponse findAuthResponseByEmail(String email) {
        User u = userRepository.findByEmailNative(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return AuthResponse.builder()
                .userId(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .mobile(u.getMobile())
                .companyName(u.getCompanyName())
                .representing(u.getRepresenting())
                .newUser(false)
                .build();
    }
}