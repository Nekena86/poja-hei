package com.hei.school.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateExamRequest(
        @NotBlank String ref,
        @NotNull UUID courseTeachingId,
        @NotNull Instant dateExam,
        @NotNull @DecimalMin("0.1") BigDecimal coefficient,
        @Min(1) @Max(3) int academicYear) {}
