package com.hei.school.service.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Assigns one teacher to teach one course to one group. */
public record CreateCourseTeachingRequest(
    @NotNull UUID courseId, @NotNull UUID teacherId, @NotNull UUID groupId) {}
