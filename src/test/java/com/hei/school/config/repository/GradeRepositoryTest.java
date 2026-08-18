package com.hei.school.config.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.hei.school.entity.Course;
import com.hei.school.entity.CourseTeaching;
import com.hei.school.entity.Exam;
import com.hei.school.entity.Grade;
import com.hei.school.entity.Group;
import com.hei.school.entity.Role;
import com.hei.school.entity.User;
import com.hei.school.repository.*;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("local")
class GradeRepositoryTest {

  @Autowired private UserRepository userRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private CourseTeachingRepository courseTeachingRepository;
  @Autowired private ExamRepository examRepository;
  @Autowired private GradeRepository gradeRepository;

  @Test
  void findAllForPromotionOnlyReturnsGradesOfStudentsInThatPromotion() {
    User teacher =
        userRepository.save(
            User.builder()
                .email("repo-teacher@school.io")
                .password("x")
                .firstName("F")
                .lastName("L")
                .role(Role.TEACHER)
                .build());
    User student2024 =
        userRepository.save(
            User.builder()
                .email("repo-student-2024@school.io")
                .password("x")
                .firstName("F")
                .lastName("L")
                .role(Role.STUDENT)
                .promotionYear(2024)
                .build());
    User student2023 =
        userRepository.save(
            User.builder()
                .email("repo-student-2023@school.io")
                .password("x")
                .firstName("F")
                .lastName("L")
                .role(Role.STUDENT)
                .promotionYear(2023)
                .build());

    Course course =
        courseRepository.save(Course.builder().ref("REPO-C1").title("Course").credits(6).build());
    Group group = groupRepository.save(Group.builder().ref("REPO-G1").build());
    CourseTeaching teaching =
        courseTeachingRepository.save(
            CourseTeaching.builder().course(course).teacher(teacher).group(group).build());
    Exam exam =
        examRepository.save(
            Exam.builder()
                .ref("REPO-E1")
                .courseTeaching(teaching)
                .dateExam(Instant.now())
                .coefficient(BigDecimal.ONE)
                .build());

    gradeRepository.save(
        Grade.builder()
            .student(student2024)
            .exam(exam)
            .value(BigDecimal.TEN)
            .lastModifiedAt(Instant.now())
            .lastModifiedBy("x")
            .build());
    gradeRepository.save(
        Grade.builder()
            .student(student2023)
            .exam(exam)
            .value(BigDecimal.valueOf(8))
            .lastModifiedAt(Instant.now())
            .lastModifiedBy("x")
            .build());

    var grades = gradeRepository.findAllForPromotion(2024);

    // The demo dataset also holds 2024 students, so assert on membership rather than on a
    // total: the 2024 student's grade is in, the 2023 one is not, and nothing else leaks in.
    assertThat(grades)
        .extracting(g -> g.getStudent().getEmail())
        .contains("repo-student-2024@school.io");
    assertThat(grades)
        .extracting(g -> g.getStudent().getEmail())
        .doesNotContain("repo-student-2023@school.io");
    assertThat(grades)
        .allSatisfy(g -> assertThat(g.getStudent().getPromotionYear()).isEqualTo(2024));
  }
}
