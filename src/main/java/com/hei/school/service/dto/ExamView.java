package com.hei.school.service.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ExamView(
        UUID id,
        String ref,
        String courseTitle,
        Instant dateExam,
        BigDecimal coefficient,
        int academicYear) {}
