package com.recruitment.service;

import com.recruitment.dto.AuthResponse;
import com.recruitment.dto.LoginRequest;
import com.recruitment.dto.RegisterRequest;
import com.recruitment.enums.RoleType;
import com.recruitment.mapper.UserMapper;
import com.recruitment.model.Role;
import com.recruitment.model.User;
import com.recruitment.repository.RoleRepository;
import com.recruitment.repository.UserRepository;
import com.recruitment.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        Role candidateRole = roleRepository.findByName(RoleType.CANDIDATE)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(RoleType.CANDIDATE).build()));

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(candidateRole));

        userRepository.save(user);

        log.info("New user registered: {}", user.getEmail());
        return generateAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        log.info("User logged in: {}", request.email());
        return generateAuthResponse(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        AuthResponse response = userMapper.toAuthResponse(user);
        String token = jwtTokenProvider.generateToken(user.getEmail(), response.roles());
        return new AuthResponse(token, response.email(), response.roles());
    }
}
