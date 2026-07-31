package com.recruitment.mapper;

import com.recruitment.dto.AdminUserRequest;
import com.recruitment.dto.AuthResponse;
import com.recruitment.dto.RegisterRequest;
import com.recruitment.dto.UserResponse;
import com.recruitment.model.Role;
import com.recruitment.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(RegisterRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(AdminUserRequest request);

    @Mapping(target = "token", ignore = true)
    AuthResponse toAuthResponse(User user);

    UserResponse toUserResponse(User user);

    default Set<String> map(Set<Role> roles) {
        return roles.stream()
                .map(role -> "ROLE_" + role.getName().name())
                .collect(Collectors.toSet());
    }
}
