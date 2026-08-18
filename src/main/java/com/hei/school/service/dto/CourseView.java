package com.hei.school.service.dto;

import java.util.UUID;

public record CourseView(UUID id, String ref, String title, int credits) {}

