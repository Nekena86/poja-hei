package com.hei.school.repository;

import com.hei.school.entity.Exam;
import com.hei.school.entity.Grade;
import com.hei.school.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GradeRepository extends JpaRepository<Grade, UUID> {
  List<Grade> findByStudent(User student);

  boolean existsByStudentAndExam(User student, Exam exam);

  @Query(
      "select g from Grade g where g.student.role = com.hei.school.entity.Role.STUDENT "
          + "and g.student.promotionYear = :promotionYear")
  List<Grade> findAllForPromotion(@Param("promotionYear") Integer promotionYear);
}
