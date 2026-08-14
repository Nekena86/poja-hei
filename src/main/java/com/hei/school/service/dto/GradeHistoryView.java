package com.hei.school.service.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record GradeHistoryView(
        BigDecimal previousValue,
        BigDecimal newValue,
        String reason,
        String changedBy,
        Instant changedAt) {}

