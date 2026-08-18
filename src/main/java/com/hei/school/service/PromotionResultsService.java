package com.hei.school.service;

import com.hei.school.entity.Grade;
import com.hei.school.entity.Role;
import com.hei.school.entity.User;
import com.hei.school.repository.GradeRepository;
import com.hei.school.repository.UserRepository;
import com.hei.school.service.dto.AcademicYearResult;
import com.hei.school.service.dto.PromotionResults;
import com.hei.school.service.dto.StudentPromotionResult;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A promotion's results across the three years of the curriculum — the admin-only view the brief
 * asks for. Each year is averaged on its own so a weak year stays visible instead of being diluted
 * into the overall average.
 */
@Service
@AllArgsConstructor
public class PromotionResultsService {

  /** A HEI promotion spans three academic years. */
  public static final int PROMOTION_LENGTH_YEARS = 3;

  private final UserRepository userRepository;
  private final GradeRepository gradeRepository;
  private final TranscriptService transcriptService;

  @Transactional(readOnly = true)
  public PromotionResults getResults(int promotionYear) {
    List<User> students = userRepository.findByRoleAndPromotionYear(Role.STUDENT, promotionYear);
    Map<User, List<Grade>> gradesByStudent =
        gradeRepository.findAllForPromotion(promotionYear).stream()
            .collect(Collectors.groupingBy(Grade::getStudent));

    List<StudentPromotionResult> rows = new ArrayList<>();
    int graduateCount = 0;
    for (User student : students) {
      List<Grade> studentGrades = gradesByStudent.getOrDefault(student, List.of());
      Map<Integer, List<Grade>> gradesByAcademicYear =
          studentGrades.stream().collect(Collectors.groupingBy(g -> g.getExam().getAcademicYear()));

      List<AcademicYearResult> perAcademicYear = new ArrayList<>(PROMOTION_LENGTH_YEARS);
      for (int academicYear = 1; academicYear <= PROMOTION_LENGTH_YEARS; academicYear++) {
        List<Grade> yearGrades = gradesByAcademicYear.getOrDefault(academicYear, List.of());
        perAcademicYear.add(
            new AcademicYearResult(
                academicYear, transcriptService.weightedAverage(yearGrades), yearGrades.size()));
      }

      BigDecimal overall = transcriptService.weightedAverage(studentGrades);
      boolean graduated = overall.compareTo(GraduatesExcelService.PASSING_AVERAGE) >= 0;
      if (graduated) {
        graduateCount++;
      }
      rows.add(
          new StudentPromotionResult(
              StudentIdentifier.stdOf(student),
              student.getLastName(),
              student.getFirstName(),
              perAcademicYear,
              overall,
              graduated));
    }

    rows.sort(Comparator.comparing(StudentPromotionResult::overallAverage).reversed());
    return new PromotionResults(promotionYear, students.size(), graduateCount, rows);
  }
}
