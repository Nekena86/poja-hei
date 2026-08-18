package com.hei.school.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateCourseRequest(
        @NotBlank String ref, @NotBlank String title, @Min(1) int credits) {}
