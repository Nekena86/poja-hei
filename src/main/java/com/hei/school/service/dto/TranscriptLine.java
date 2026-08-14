package com.hei.school.service.dto;

import java.math.BigDecimal;

public record TranscriptLine(
        String courseTitle, String examRef, BigDecimal coefficient, BigDecimal value) {}

