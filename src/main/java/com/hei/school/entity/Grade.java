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
@Table(
    name = "grade",
    uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "exam_id"}))
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Grade {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "student_id")
  private User student;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "exam_id")
  private Exam exam;

  @Column(nullable = false, precision = 5, scale = 2)
  private BigDecimal value;

  @Column(nullable = false)
  private Instant lastModifiedAt;

  @Column(nullable = false)
  private String lastModifiedBy;
}
