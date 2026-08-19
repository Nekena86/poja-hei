package com.hei.school.service.dto;

import com.hei.school.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, message = "password must be at least 8 characters") String password,
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotNull Role role,
    Integer promotionYear) {}
