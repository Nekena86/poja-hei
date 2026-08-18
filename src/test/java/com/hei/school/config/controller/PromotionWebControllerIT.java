package com.hei.school.config.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hei.school.entity.Role;
import com.hei.school.entity.User;
import com.hei.school.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class PromotionWebControllerIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private User admin;
  private User student;

  @BeforeEach
  void setUp() {
    admin =
        userRepository.save(
            User.builder()
                .email("web-admin@school.io")
                .password(passwordEncoder.encode("password"))
                .firstName("A")
                .lastName("B")
                .role(Role.ADMIN)
                .build());
    student =
        userRepository.save(
            User.builder()
                .email("web-student@school.io")
                .password(passwordEncoder.encode("password"))
                .firstName("A")
                .lastName("B")
                .role(Role.STUDENT)
                .promotionYear(2024)
                .build());
  }

  @Test
  void adminCanSeeThePromotionsPage() throws Exception {
    mockMvc
        .perform(get("/promotions").with(httpBasic(admin.getEmail(), "password")))
        .andExpect(status().isOk());
  }

  @Test
  void studentCannotSeeThePromotionsPage() throws Exception {
    mockMvc
        .perform(get("/promotions").with(httpBasic(student.getEmail(), "password")))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminCanDownloadTheGraduatesExcelFile() throws Exception {
    mockMvc
        .perform(
            get("/api/promotions/{year}/graduates.xlsx", 2024)
                .with(httpBasic(admin.getEmail(), "password")))
        .andExpect(status().isOk());
  }

  @Test
  void studentCannotDownloadTheGraduatesExcelFile() throws Exception {
    mockMvc
        .perform(
            get("/api/promotions/{year}/graduates.xlsx", 2024)
                .with(httpBasic(student.getEmail(), "password")))
        .andExpect(status().isForbidden());
  }
}
