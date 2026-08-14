package com.hei.school.service.dto;

import java.math.BigDecimal;

public record GraduateRow(
        int rang, String std, String nom, String prenom, BigDecimal moyenneGenerale) {}

