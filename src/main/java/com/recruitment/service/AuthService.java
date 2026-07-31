package com.recruitment.service;

import com.recruitment.dto.AuthResponse;
import com.recruitment.dto.LoginRequest;
import com.recruitment.dto.RegisterRequest;
import com.recruitment.enums.RoleType;
import com.recruitment.model.Role;
import com.recruitment.model.User;
import com.recruitment.repository.RoleRepository;
import com.recruitment.repository.UserRepository;
import com.recruitment.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        Role candidateRole = roleRepository.findByName(RoleType.CANDIDATE)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(RoleType.CANDIDATE).build()));

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(candidateRole))
                .build();

        userRepository.save(user);

        return generateAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return generateAuthResponse(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getName().name())
                .collect(Collectors.toSet());

        String token = jwtTokenProvider.generateToken(user.getEmail(), roles);

        return new AuthResponse(token, user.getEmail(), roles);
    }
}
