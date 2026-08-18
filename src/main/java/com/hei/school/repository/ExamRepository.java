package com.hei.school.repository;

import com.hei.school.entity.Exam;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, UUID> {
  boolean existsByRef(String ref);
}
