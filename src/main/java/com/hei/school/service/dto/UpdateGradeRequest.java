package com.hei.school.service.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateGradeRequest(
    @NotNull @DecimalMin("0.0") @DecimalMax("20.0") BigDecimal value, @NotBlank String reason) {}
