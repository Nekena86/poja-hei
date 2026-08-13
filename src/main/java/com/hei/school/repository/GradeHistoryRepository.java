package com.hei.school.repository;

import com.hei.school.entity.Grade;
import com.hei.school.entity.GradeHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeHistoryRepository extends JpaRepository<GradeHistory, UUID> {
  List<GradeHistory> findByGradeOrderByChangedAtAsc(Grade grade);
}
