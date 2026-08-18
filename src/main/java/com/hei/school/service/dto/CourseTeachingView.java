package com.hei.school.service.dto;

import java.util.UUID;

public record CourseTeachingView(
        UUID id, String courseRef, String courseTitle, String teacherEmail, String groupRef) {}
