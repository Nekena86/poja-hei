package com.hei.school.repository;

import com.hei.school.entity.StudentGroupHistory;
import com.hei.school.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentGroupHistoryRepository extends JpaRepository<StudentGroupHistory, UUID> {

  Optional<StudentGroupHistory> findByStudentAndEndDateIsNull(User student);

  List<StudentGroupHistory> findByStudentOrderByStartDateAsc(User student);
}
