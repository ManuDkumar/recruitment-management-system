package com.recruitment.config;

import com.recruitment.enums.RoleType;
import com.recruitment.model.Role;
import com.recruitment.model.User;
import com.recruitment.repository.RoleRepository;
import com.recruitment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@recruitment.com}")
    private String adminEmail;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.interviewer.email:interviewer@recruitment.com}")
    private String interviewerEmail;

    @Value("${app.interviewer.password:interviewer123}")
    private String interviewerPassword;

    @Override
    public void run(ApplicationArguments args) {
        Role adminRole = roleRepository.findByName(RoleType.ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleType.ADMIN).build()));
        Role interviewerRole = roleRepository.findByName(RoleType.INTERVIEWER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleType.INTERVIEWER).build()));

        if (!userRepository.existsByEmail(adminEmail)) {
            userRepository.save(User.builder()
                    .name("System Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .roles(Set.of(adminRole))
                    .build());
        }

        if (!userRepository.existsByEmail(interviewerEmail)) {
            userRepository.save(User.builder()
                    .name("Test Interviewer")
                    .email(interviewerEmail)
                    .password(passwordEncoder.encode(interviewerPassword))
                    .roles(Set.of(interviewerRole))
                    .build());
        }
    }
}
