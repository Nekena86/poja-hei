package com.hei.school.service.dto;

import com.hei.school.entity.Role;
import java.util.UUID;

public record UserView(
    UUID id,
    String std,
    String email,
    String firstName,
    String lastName,
    Role role,
    Integer promotionYear) {}
