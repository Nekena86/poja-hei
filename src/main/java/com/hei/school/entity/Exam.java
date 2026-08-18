package com.hei.school.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exam")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Exam {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String ref;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "course_teaching_id")
  private CourseTeaching courseTeaching;

  @Column(nullable = false)
  private Instant dateExam;

  @Column(nullable = false)
  private BigDecimal coefficient;

  /**
   * Which of the promotion's three years this exam belongs to (1, 2 or 3). Needed to report a
   * promotion's results year by year, which a date alone cannot tell us.
   */
  @Column(name = "academic_year", nullable = false)
  private int academicYear;
}
