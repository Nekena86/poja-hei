package com.hei.school.service;

import com.hei.school.entity.Grade;
import com.hei.school.entity.User;
import com.hei.school.repository.GradeRepository;
import com.hei.school.service.dto.TranscriptLine;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TranscriptService {

  private final GradeRepository gradeRepository;

  @Transactional(readOnly = true)
  public List<TranscriptLine> getTranscriptLines(User student) {
    return gradeRepository.findByStudent(student).stream()
            .map(
                    g ->
                            new TranscriptLine(
                                    g.getExam().getCourseTeaching().getCourse().getTitle(),
                                    g.getExam().getRef(),
                                    g.getExam().getCoefficient(),
                                    g.getValue()))
            .toList();
  }

  public BigDecimal weightedAverage(List<Grade> grades) {
    BigDecimal totalWeight =
            grades.stream()
                    .map(g -> g.getExam().getCoefficient())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    BigDecimal weightedSum =
            grades.stream()
                    .map(g -> g.getValue().multiply(g.getExam().getCoefficient()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
    return weightedSum.divide(totalWeight, new MathContext(4, RoundingMode.HALF_UP));
  }
}
