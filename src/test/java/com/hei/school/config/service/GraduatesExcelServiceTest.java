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
import com.hei.school.service.GraduatesExcelService;
import com.hei.school.service.TranscriptService;
import com.hei.school.service.dto.GraduateRow;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraduatesExcelServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private GradeRepository gradeRepository;

  private GraduatesExcelService service;

  @BeforeEach
  void setUp() {
    service =
        new GraduatesExcelService(
            userRepository, gradeRepository, new TranscriptService(gradeRepository));
  }

  private User student(String first, String last) {
    return User.builder()
        .id(UUID.randomUUID())
        .email(first + "@school.io")
        .password("x")
        .firstName(first)
        .lastName(last)
        .role(Role.STUDENT)
        .promotionYear(2024)
        .build();
  }

  private Grade gradeFor(User student, BigDecimal value, BigDecimal coefficient) {
    Course course =
        Course.builder().id(UUID.randomUUID()).ref("C").title("Course").credits(6).build();
    Group group = Group.builder().id(UUID.randomUUID()).ref("G").build();
    User teacher =
        User.builder()
            .id(UUID.randomUUID())
            .email("t@school.io")
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
            .ref("E")
            .courseTeaching(teaching)
            .dateExam(Instant.now())
            .coefficient(coefficient)
            .build();
    return Grade.builder()
        .id(UUID.randomUUID())
        .student(student)
        .exam(exam)
        .value(value)
        .lastModifiedAt(Instant.now())
        .lastModifiedBy("x")
        .build();
  }

  @Test
  void ranksGraduatesByDescendingAverage() {
    User top = student("Alice", "Top");
    User middle = student("Bob", "Middle");

    when(userRepository.findByRoleAndPromotionYear(Role.STUDENT, 2024))
        .thenReturn(List.of(middle, top));
    when(gradeRepository.findAllForPromotion(2024))
        .thenReturn(
            List.of(
                gradeFor(top, BigDecimal.valueOf(18), BigDecimal.ONE),
                gradeFor(middle, BigDecimal.valueOf(12), BigDecimal.ONE)));

    var rows = service.computeGraduateRows(2024);

    assertThat(rows).hasSize(2);
    assertThat(rows.get(0).rang()).isEqualTo(1);
    assertThat(rows.get(0).prenom()).isEqualTo("Alice");
    assertThat(rows.get(1).rang()).isEqualTo(2);
    assertThat(rows.get(1).prenom()).isEqualTo("Bob");
  }

  @Test
  void listsOnlyStudentsWhoReachedThePassMark() {
    User graduate = student("Alice", "Top");
    User failed = student("Bob", "Bottom");
    User ungraded = student("Chloe", "Absent");

    when(userRepository.findByRoleAndPromotionYear(Role.STUDENT, 2024))
        .thenReturn(List.of(graduate, failed, ungraded));
    when(gradeRepository.findAllForPromotion(2024))
        .thenReturn(
            List.of(
                gradeFor(graduate, BigDecimal.valueOf(18), BigDecimal.ONE),
                gradeFor(failed, BigDecimal.valueOf(9.99), BigDecimal.ONE)));

    var rows = service.computeGraduateRows(2024);

    assertThat(rows).extracting(GraduateRow::prenom).containsExactly("Alice");
  }

  @Test
  void keepsStudentsExactlyOnThePassMark() {
    User borderline = student("Dina", "Exact");
    when(userRepository.findByRoleAndPromotionYear(Role.STUDENT, 2024))
        .thenReturn(List.of(borderline));
    when(gradeRepository.findAllForPromotion(2024))
        .thenReturn(List.of(gradeFor(borderline, BigDecimal.valueOf(10), BigDecimal.ONE)));

    assertThat(service.computeGraduateRows(2024)).hasSize(1);
  }

  @Test
  void workbookContainsExpectedHeaderAndRows() throws Exception {
    User top = student("Alice", "Top");
    when(userRepository.findByRoleAndPromotionYear(Role.STUDENT, 2024)).thenReturn(List.of(top));
    when(gradeRepository.findAllForPromotion(2024))
        .thenReturn(List.of(gradeFor(top, BigDecimal.valueOf(15), BigDecimal.ONE)));

    var rows = service.computeGraduateRows(2024);
    byte[] bytes = service.toWorkbook(rows);

    try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
      var sheet = workbook.getSheetAt(0);
      var header = sheet.getRow(0);
      assertThat(header.getCell(0).getStringCellValue()).isEqualTo("rang");
      assertThat(header.getCell(1).getStringCellValue()).isEqualTo("STD");
      assertThat(header.getCell(2).getStringCellValue()).isEqualTo("nom");
      assertThat(header.getCell(3).getStringCellValue()).isEqualTo("prenom");
      assertThat(header.getCell(4).getStringCellValue()).isEqualTo("moyenne generale");

      var firstRow = sheet.getRow(1);
      assertThat(firstRow.getCell(3).getStringCellValue()).isEqualTo("Alice");
    }
  }
}
