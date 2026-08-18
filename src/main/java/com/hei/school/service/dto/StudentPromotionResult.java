package com.hei.school.service.dto;

import java.math.BigDecimal;
import java.util.List;

public record StudentPromotionResult(
        String std,
        String nom,
        String prenom,
        List<AcademicYearResult> perAcademicYear,
        BigDecimal overallAverage,
        boolean graduated) {}
