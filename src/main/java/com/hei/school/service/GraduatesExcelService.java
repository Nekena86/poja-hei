package com.hei.school.service;

import com.hei.school.entity.Grade;
import com.hei.school.entity.Role;
import com.hei.school.entity.User;
import com.hei.school.repository.GradeRepository;
import com.hei.school.repository.UserRepository;
import com.hei.school.service.dto.GraduateRow;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Builds the "liste de diplomes" Excel export for a given promotion (entry year): rang, STD (the
 * student's identifier), nom, prenom, moyenne generale — ranked by descending average.
 *
 * <p>Only students who actually graduated are listed: a weighted average of at least {@link
 * #PASSING_AVERAGE} out of 20. Students with no grades at all therefore never appear.
 */
@Service
@AllArgsConstructor
public class GraduatesExcelService {

  /** Pass mark out of 20 that turns a student of the promotion into a graduate. */
  public static final BigDecimal PASSING_AVERAGE = BigDecimal.TEN;

  private final UserRepository userRepository;
  private final GradeRepository gradeRepository;
  private final TranscriptService transcriptService;

  @Transactional(readOnly = true)
  public List<GraduateRow> computeGraduateRows(int promotionYear) {
    List<User> students = userRepository.findByRoleAndPromotionYear(Role.STUDENT, promotionYear);
    List<Grade> promotionGrades = gradeRepository.findAllForPromotion(promotionYear);
    Map<User, List<Grade>> gradesByStudent =
        promotionGrades.stream().collect(Collectors.groupingBy(Grade::getStudent));

    List<GraduateRow> rows = new ArrayList<>();
    for (User student : students) {
      List<Grade> grades = gradesByStudent.getOrDefault(student, List.of());
      BigDecimal average = transcriptService.weightedAverage(grades);
      if (average.compareTo(PASSING_AVERAGE) < 0) {
        continue;
      }
      rows.add(
          new GraduateRow(
              0,
              StudentIdentifier.stdOf(student),
              student.getLastName(),
              student.getFirstName(),
              average));
    }

    rows.sort(Comparator.comparing(GraduateRow::moyenneGenerale).reversed());

    List<GraduateRow> ranked = new ArrayList<>(rows.size());
    for (int i = 0; i < rows.size(); i++) {
      GraduateRow r = rows.get(i);
      ranked.add(new GraduateRow(i + 1, r.std(), r.nom(), r.prenom(), r.moyenneGenerale()));
    }
    return ranked;
  }

  public byte[] toWorkbook(List<GraduateRow> rows) {
    try (XSSFWorkbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      XSSFSheet sheet = workbook.createSheet("Diplomes");

      Row header = sheet.createRow(0);
      String[] headers = {"rang", "STD", "nom", "prenom", "moyenne generale"};
      for (int i = 0; i < headers.length; i++) {
        Cell cell = header.createCell(i);
        cell.setCellValue(headers[i]);
      }

      int rowIndex = 1;
      for (GraduateRow row : rows) {
        Row excelRow = sheet.createRow(rowIndex++);
        excelRow.createCell(0).setCellValue(row.rang());
        excelRow.createCell(1).setCellValue(row.std());
        excelRow.createCell(2).setCellValue(row.nom());
        excelRow.createCell(3).setCellValue(row.prenom());
        excelRow.createCell(4).setCellValue(row.moyenneGenerale().doubleValue());
      }

      for (int i = 0; i < headers.length; i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(out);
      return out.toByteArray();
    } catch (java.io.IOException e) {
      throw new RuntimeException("Failed to build the graduates workbook", e);
    }
  }
}
