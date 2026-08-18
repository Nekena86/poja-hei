package com.hei.school.repository;

import com.hei.school.entity.Course;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, UUID> {
  boolean existsByRef(String ref);
}
