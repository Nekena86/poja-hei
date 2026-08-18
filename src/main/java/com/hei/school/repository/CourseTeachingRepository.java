package com.hei.school.repository;

import com.hei.school.entity.Course;
import com.hei.school.entity.CourseTeaching;
import com.hei.school.entity.Group;
import com.hei.school.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseTeachingRepository extends JpaRepository<CourseTeaching, UUID> {
  List<CourseTeaching> findByTeacher(User teacher);

  boolean existsByCourseAndTeacherAndGroup(Course course, User teacher, Group group);
}
