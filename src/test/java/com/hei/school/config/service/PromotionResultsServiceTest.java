package com.hei.school.config.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.hei.school.entity.Course;
import com.hei.school.entity.CourseTeaching;
import com.hei.school.entity.Exam;
import com.hei.school.entity.Grade;
import com.hei.school.entity.Group;
import com.hei.school.entity.Role;
import com.hei.school.entity.User;
import com.hei.school.repository.GradeRepository;
import com.hei.school.repository.UserRepository;
import com.hei.school.service.PromotionResultsService;
import com.hei.school.service.TranscriptService;
import com.hei.school.service.dto.StudentPromotionResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PromotionResultsServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private GradeRepository gradeRepository;

  private PromotionResultsService service;

  @BeforeEach
  void setUp() {
    service =
        new PromotionResultsService(
            userRepository, gradeRepository, new TranscriptService(gradeRepository));
  }

  private User student(String first) {
    return User.builder()
        .id(UUID.randomUUID())
        .email(first + "@hei.school")
        .password("x")
        .firstName(first)
        .lastName(first.toUpperCase())
        .role(Role.STUDENT)
        .promotionYear(2022)
        .build();
  }

  private Grade grade(User student, int academicYear, String value, String coefficient) {
    Course course =
        Course.builder().id(UUID.randomUUID()).ref("C").title("Course").credits(6).build();
    Group group = Group.builder().id(UUID.randomUUID()).ref("G").build();
    User teacher =
        User.builder()
            .id(UUID.randomUUID())
            .email("t@hei.school")
            .password("x")
            .firstName("T")
            .lastName("T")
            .role(Role.TEACHER)
            .build();
    CourseTeaching teaching =
        CourseTeaching.builder()
            .id(UUID.randomUUID())
            .course(course)
            .teacher(teacher)
            .group(group)
            .build();
    Exam exam =
        Exam.builder()
            .id(UUID.randomUUID())
            .ref("E-" + academicYear)
            .courseTeaching(teaching)
            .dateExam(Instant.now())
            .coefficient(new BigDecimal(coefficient))
            .academicYear(academicYear)
            .build();
    return Grade.builder()
        .id(UUID.randomUUID())
        .student(student)
        .exam(exam)
        .value(new BigDecimal(value))
        .lastModifiedAt(Instant.now())
        .lastModifiedBy("t@hei.school")
        .build();
  }

  @Test
  void reportsEachOfTheThreeYearsSeparatelyAndRanksByOverallAverage() {
    User strong = student("strong");
    User weak = student("weak");
    when(userRepository.findByRoleAndPromotionYear(Role.STUDENT, 2022))
        .thenReturn(List.of(weak, strong));
    when(gradeRepository.findAllForPromotion(2022))
        .thenReturn(
            List.of(
                grade(strong, 1, "16", "2"),
                grade(strong, 2, "15", "3"),
                grade(strong, 3, "14", "2"),
                grade(weak, 1, "8", "2"),
                grade(weak, 2, "7", "3"),
                grade(weak, 3, "9", "2")));

    var results = service.getResults(2022);

    assertThat(results.promotionYear()).isEqualTo(2022);
    assertThat(results.studentCount()).isEqualTo(2);
    assertThat(results.graduateCount()).isEqualTo(1);
    assertThat(results.students())
        .extracting(StudentPromotionResult::prenom)
        .containsExactly("strong", "weak");

    var top = results.students().get(0);
    assertThat(top.overallAverage()).isEqualByComparingTo("15.00");
    assertThat(top.graduated()).isTrue();
    assertThat(top.perAcademicYear()).hasSize(3);
    assertThat(top.perAcademicYear().get(0).average()).isEqualByComparingTo("16");
    assertThat(top.perAcademicYear().get(1).average()).isEqualByComparingTo("15");
    assertThat(top.perAcademicYear().get(2).average()).isEqualByComparingTo("14");
    assertThat(top.perAcademicYear().get(0).gradeCount()).isEqualTo(1);

    assertThat(results.students().get(1).graduated()).isFalse();
  }

  @Test
  void aYearWithoutAnyExamReportsZeroRatherThanFailing() {
    User onlyFirstYear = student("first");
    when(userRepository.findByRoleAndPromotionYear(Role.STUDENT, 2024))
        .thenReturn(List.of(onlyFirstYear));
    when(gradeRepository.findAllForPromotion(2024))
        .thenReturn(List.of(grade(onlyFirstYear, 1, "12", "1")));

    var results = service.getResults(2024);
    var student = results.students().get(0);

    assertThat(student.perAcademicYear()).hasSize(3);
    assertThat(student.perAcademicYear().get(1).average()).isEqualByComparingTo("0");
    assertThat(student.perAcademicYear().get(1).gradeCount()).isZero();
    assertThat(student.overallAverage()).isEqualByComparingTo("12");
  }

  @Test
  void aStudentWithNoGradeAtAllDoesNotGraduate() {
    User ungraded = student("ungraded");
    when(userRepository.findByRoleAndPromotionYear(Role.STUDENT, 2022))
        .thenReturn(List.of(ungraded));
    when(gradeRepository.findAllForPromotion(2022)).thenReturn(List.of());

    var results = service.getResults(2022);

    assertThat(results.graduateCount()).isZero();
    assertThat(results.students().get(0).graduated()).isFalse();
    assertThat(results.students().get(0).std()).isNotBlank();
  }
}
