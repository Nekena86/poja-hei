package com.hei.school.config.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hei.school.entity.Course;
import com.hei.school.entity.CourseTeaching;
import com.hei.school.entity.Exam;
import com.hei.school.entity.Grade;
import com.hei.school.entity.Group;
import com.hei.school.entity.Role;
import com.hei.school.entity.User;
import com.hei.school.repository.CourseRepository;
import com.hei.school.repository.CourseTeachingRepository;
import com.hei.school.repository.ExamRepository;
import com.hei.school.repository.GradeRepository;
import com.hei.school.repository.GroupRepository;
import com.hei.school.repository.UserRepository;
import com.hei.school.service.dto.UpdateGradeRequest;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class GradeControllerIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private CourseTeachingRepository courseTeachingRepository;
  @Autowired private ExamRepository examRepository;
  @Autowired private GradeRepository gradeRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private ObjectMapper objectMapper;

  private User student;
  private User teacher;
  private User otherStudent;
  private Grade grade;

  @BeforeEach
  void setUp() {
    teacher = userRepository.save(newUser("it-teacher@school.io", Role.TEACHER, null));
    student = userRepository.save(newUser("it-student@school.io", Role.STUDENT, 2024));
    otherStudent = userRepository.save(newUser("it-other-student@school.io", Role.STUDENT, 2024));

    Course course =
        courseRepository.save(
            Course.builder()
                .ref("IT-C1-" + System.nanoTime())
                .title("Test course")
                .credits(6)
                .build());
    Group group = groupRepository.save(Group.builder().ref("IT-G1-" + System.nanoTime()).build());
    CourseTeaching teaching =
        courseTeachingRepository.save(
            CourseTeaching.builder().course(course).teacher(teacher).group(group).build());
    Exam exam =
        examRepository.save(
            Exam.builder()
                .ref("IT-E1-" + System.nanoTime())
                .courseTeaching(teaching)
                .dateExam(Instant.now())
                .coefficient(BigDecimal.ONE)
                .build());
    grade =
        gradeRepository.save(
            Grade.builder()
                .student(student)
                .exam(exam)
                .value(BigDecimal.valueOf(12))
                .lastModifiedAt(Instant.now())
                .lastModifiedBy("seed")
                .build());
  }

  private User newUser(String email, Role role, Integer promotionYear) {
    return User.builder()
        .email(email)
        .password(passwordEncoder.encode("password"))
        .firstName("F")
        .lastName("L")
        .role(role)
        .promotionYear(promotionYear)
        .build();
  }

  @Test
  void studentCanFetchTheirOwnGrades() throws Exception {
    mockMvc
        .perform(
            get("/api/students/{id}/grades", student.getId())
                .with(httpBasic(student.getEmail(), "password")))
        .andExpect(status().isOk());
  }

  @Test
  void studentCannotFetchAnotherStudentsGrades() throws Exception {
    mockMvc
        .perform(
            get("/api/students/{id}/grades", otherStudent.getId())
                .with(httpBasic(student.getEmail(), "password")))
        .andExpect(status().isForbidden());
  }

  @Test
  void anonymousRequestIsRejected() throws Exception {
    mockMvc
        .perform(get("/api/students/{id}/grades", student.getId()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void teacherCanUpdateAGradeTheyOwnAndMustProvideAReason() throws Exception {
    var request = new UpdateGradeRequest(BigDecimal.valueOf(16), "reclamation etudiant");
    mockMvc
        .perform(
            put("/api/grades/{id}", grade.getId())
                .with(httpBasic(teacher.getEmail(), "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  void updatingAGradeWithoutAReasonIsRejected() throws Exception {
    String body = "{\"value\": 16, \"reason\": \"\"}";
    mockMvc
        .perform(
            put("/api/grades/{id}", grade.getId())
                .with(httpBasic(teacher.getEmail(), "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
  }
}
