package com.hei.school.config.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.hei.school.entity.Course;
import com.hei.school.entity.CourseTeaching;
import com.hei.school.entity.Exam;
import com.hei.school.entity.Grade;
import com.hei.school.entity.Group;
import com.hei.school.entity.Role;
import com.hei.school.entity.User;
import com.hei.school.exception.ForbiddenOperationException;
import com.hei.school.repository.GradeHistoryRepository;
import com.hei.school.repository.GradeRepository;
import com.hei.school.service.GradeService;
import com.hei.school.service.dto.UpdateGradeRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

  @Mock private GradeRepository gradeRepository;
  @Mock private GradeHistoryRepository gradeHistoryRepository;
  @Mock private com.hei.school.repository.ExamRepository examRepository;
  @Mock private com.hei.school.repository.UserRepository userRepository;

  private GradeService gradeService;

  private User student;
  private User otherStudent;
  private User owningTeacher;
  private User otherTeacher;
  private User admin;
  private Grade grade;

  @BeforeEach
  void setUp() {
    gradeService =
        new GradeService(gradeRepository, gradeHistoryRepository, examRepository, userRepository);

    student = user(Role.STUDENT, 2024);
    otherStudent = user(Role.STUDENT, 2024);
    owningTeacher = user(Role.TEACHER, null);
    otherTeacher = user(Role.TEACHER, null);
    admin = user(Role.ADMIN, null);

    Course course =
        Course.builder().id(UUID.randomUUID()).ref("C1").title("Algo").credits(6).build();
    Group group = Group.builder().id(UUID.randomUUID()).ref("L3-A").build();
    CourseTeaching teaching =
        CourseTeaching.builder()
            .id(UUID.randomUUID())
            .course(course)
            .teacher(owningTeacher)
            .group(group)
            .build();
    Exam exam =
        Exam.builder()
            .id(UUID.randomUUID())
            .ref("E1")
            .courseTeaching(teaching)
            .dateExam(Instant.now())
            .coefficient(BigDecimal.ONE)
            .build();
    grade =
        Grade.builder()
            .id(UUID.randomUUID())
            .student(student)
            .exam(exam)
            .value(BigDecimal.TEN)
            .lastModifiedAt(Instant.now())
            .lastModifiedBy("seed")
            .build();
  }

  private User user(Role role, Integer promotionYear) {
    return User.builder()
        .id(UUID.randomUUID())
        .email(role + "-" + UUID.randomUUID() + "@school.io")
        .password("x")
        .firstName("F")
        .lastName("L")
        .role(role)
        .promotionYear(promotionYear)
        .build();
  }

  @Test
  void studentCanViewOwnGrades() {
    when(gradeRepository.findByStudent(student)).thenReturn(List.of(grade));

    var result = gradeService.getGradesForStudent(student, student);

    assertThat(result).hasSize(1);
  }

  @Test
  void studentCannotViewAnotherStudentsGrades() {
    assertThatThrownBy(() -> gradeService.getGradesForStudent(student, otherStudent))
        .isInstanceOf(ForbiddenOperationException.class);
  }

  @Test
  void teacherOnlySeesGradesOfCoursesTheyTeach() {
    when(gradeRepository.findByStudent(student)).thenReturn(List.of(grade));

    var seenByOwner = gradeService.getGradesForStudent(student, owningTeacher);
    var seenByOther = gradeService.getGradesForStudent(student, otherTeacher);

    assertThat(seenByOwner).hasSize(1);
    assertThat(seenByOther).isEmpty();
  }

  @Test
  void adminSeesAllGrades() {
    when(gradeRepository.findByStudent(student)).thenReturn(List.of(grade));

    var result = gradeService.getGradesForStudent(student, admin);

    assertThat(result).hasSize(1);
  }

  @Test
  void studentCannotUpdateAGrade() {
    assertThatThrownBy(
            () ->
                gradeService.updateGrade(
                    grade.getId(),
                    new UpdateGradeRequest(BigDecimal.valueOf(18), "reclamation"),
                    student))
        .isInstanceOf(ForbiddenOperationException.class);
  }

  @Test
  void otherTeacherCannotUpdateAGradeTheyDoNotTeach() {
    when(gradeRepository.findById(grade.getId())).thenReturn(Optional.of(grade));

    assertThatThrownBy(
            () ->
                gradeService.updateGrade(
                    grade.getId(),
                    new UpdateGradeRequest(BigDecimal.valueOf(18), "reclamation"),
                    otherTeacher))
        .isInstanceOf(ForbiddenOperationException.class);
  }

  @Test
  void updatingAGradeRecordsHistoryWithReason() {
    when(gradeRepository.findById(grade.getId())).thenReturn(Optional.of(grade));
    when(gradeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var updated =
        gradeService.updateGrade(
            grade.getId(),
            new UpdateGradeRequest(BigDecimal.valueOf(17), "erreur de saisie"),
            owningTeacher);

    assertThat(updated.value()).isEqualByComparingTo("17");
    var historyCaptor =
        org.mockito.ArgumentCaptor.forClass(com.hei.school.entity.GradeHistory.class);
    org.mockito.Mockito.verify(gradeHistoryRepository).save(historyCaptor.capture());
    assertThat(historyCaptor.getValue().getReason()).isEqualTo("erreur de saisie");
    assertThat(historyCaptor.getValue().getPreviousValue()).isEqualByComparingTo("10");
    assertThat(historyCaptor.getValue().getNewValue()).isEqualByComparingTo("17");
  }

  @Test
  void adminCanUpdateAnyGrade() {
    when(gradeRepository.findById(grade.getId())).thenReturn(Optional.of(grade));
    when(gradeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var updated =
        gradeService.updateGrade(
            grade.getId(),
            new UpdateGradeRequest(BigDecimal.valueOf(20), "correction admin"),
            admin);

    assertThat(updated.value()).isEqualByComparingTo("20");
  }
}
