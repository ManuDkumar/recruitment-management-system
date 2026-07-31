package com.recruitment.service;

import com.recruitment.dto.AdminUserRequest;
import com.recruitment.dto.UserResponse;
import com.recruitment.enums.RoleType;
import com.recruitment.mapper.UserMapper;
import com.recruitment.model.Role;
import com.recruitment.model.User;
import com.recruitment.repository.RoleRepository;
import com.recruitment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userMapper.toUserResponse(user);
    }

    public UserResponse createUser(AdminUserRequest request) {
        if (request.role() == RoleType.CANDIDATE) {
            throw new IllegalArgumentException("CANDIDATE role is not allowed here; candidates self-register");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        Role role = roleRepository.findByName(request.role())
                .orElseGet(() -> roleRepository.save(Role.builder().name(request.role()).build()));

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(role));

        User saved = userRepository.save(user);
        log.info("Admin created user {} with role {}", saved.getEmail(), request.role());
        return userMapper.toUserResponse(saved);
    }
}
