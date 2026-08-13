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
@Table(name = "grade_history")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class GradeHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "grade_id")
  private Grade grade;

  private BigDecimal previousValue;

  @Column(nullable = false)
  private BigDecimal newValue;

  @Column(nullable = false)
  private String reason;

  @Column(nullable = false)
  private String changedBy;

  @Column(nullable = false)
  private Instant changedAt;
}
