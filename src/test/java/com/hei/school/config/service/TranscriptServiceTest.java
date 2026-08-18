package com.hei.school.config.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.hei.school.entity.Exam;
import com.hei.school.entity.Grade;
import com.hei.school.repository.GradeRepository;
import com.hei.school.service.TranscriptService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class TranscriptServiceTest {

  private final TranscriptService service = new TranscriptService(mock(GradeRepository.class));

  @Test
  void weightedAverageIsWeightedByExamCoefficient() {
    Grade grade1 =
        Grade.builder().value(BigDecimal.valueOf(10)).exam(examWithCoefficient(1)).build();
    Grade grade2 =
        Grade.builder().value(BigDecimal.valueOf(20)).exam(examWithCoefficient(3)).build();

    // (10*1 + 20*3) / (1+3) = 70/4 = 17.5
    BigDecimal average = service.weightedAverage(List.of(grade1, grade2));

    assertThat(average).isEqualByComparingTo("17.5");
  }

  @Test
  void weightedAverageIsZeroWhenThereAreNoGrades() {
    assertThat(service.weightedAverage(List.of())).isEqualByComparingTo(BigDecimal.ZERO);
  }

  private Exam examWithCoefficient(int coefficient) {
    return Exam.builder().coefficient(BigDecimal.valueOf(coefficient)).build();
  }
}
