package com.hei.school.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "course_teaching",
    uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "teacher_id", "group_id"}))
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CourseTeaching {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "course_id")
  private Course course;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "teacher_id")
  private User teacher;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "group_id")
  private Group group;
}
