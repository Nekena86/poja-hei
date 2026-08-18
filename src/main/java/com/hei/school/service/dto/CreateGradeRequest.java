package com.hei.school.service.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateGradeRequest(
        @NotNull UUID studentId,
        @NotNull UUID examId,
        @NotNull @DecimalMin("0.0") @DecimalMax("20.0") BigDecimal value) {}
