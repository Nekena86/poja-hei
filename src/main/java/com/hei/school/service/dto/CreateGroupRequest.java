package com.hei.school.service.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateGroupRequest(@NotBlank String ref) {}
