package com.hei.school.service.dto;

import java.util.List;

public record PromotionResults(
        int promotionYear,
        int studentCount,
        int graduateCount,
        List<StudentPromotionResult> students) {}
