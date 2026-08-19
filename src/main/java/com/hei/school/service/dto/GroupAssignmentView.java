package com.hei.school.service.dto;

import java.time.LocalDate;
import java.util.UUID;

public record GroupAssignmentView(
    UUID id, UUID groupId, String groupRef, LocalDate startDate, LocalDate endDate) {}
