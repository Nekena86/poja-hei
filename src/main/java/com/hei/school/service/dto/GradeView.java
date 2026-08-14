package com.hei.school.service.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GradeView(
        UUID id,
        String examRef,
        String courseTitle,
        BigDecimal coefficient,
        BigDecimal value,
        Instant lastModifiedAt) {}

