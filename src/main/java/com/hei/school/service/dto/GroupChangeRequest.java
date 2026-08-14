package com.hei.school.service.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record GroupChangeRequest(@NotNull UUID newGroupId, @NotNull LocalDate effectiveDate) {}
