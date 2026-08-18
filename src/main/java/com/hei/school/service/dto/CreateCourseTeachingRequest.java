package com.hei.school.service.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateCourseTeachingRequest(
        @NotNull UUID courseId, @NotNull UUID teacherId, @NotNull UUID groupId) {}
