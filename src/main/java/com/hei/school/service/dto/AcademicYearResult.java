package com.hei.school.service.dto;

import java.math.BigDecimal;

/** One of the promotion's three years, for one student. */
public record AcademicYearResult(int academicYear, BigDecimal average, int gradeCount) {}
