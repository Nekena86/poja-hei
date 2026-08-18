package com.hei.school.service;

import com.hei.school.entity.Course;
import com.hei.school.entity.CourseTeaching;
import com.hei.school.entity.Group;
import com.hei.school.entity.Role;
import com.hei.school.entity.User;
import com.hei.school.exception.ResourceNotFoundException;
import com.hei.school.repository.CourseRepository;
import com.hei.school.repository.CourseTeachingRepository;
import com.hei.school.repository.GroupRepository;
import com.hei.school.repository.UserRepository;
import com.hei.school.service.dto.CourseTeachingView;
import com.hei.school.service.dto.CourseView;
import com.hei.school.service.dto.CreateCourseRequest;
import com.hei.school.service.dto.CreateCourseTeachingRequest;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Courses, and who teaches them to whom. A course can be taught by several teachers and given to
 * several groups — each combination is one {@link CourseTeaching}.
 */
@Service
@AllArgsConstructor
public class CourseService {

  private final CourseRepository courseRepository;
  private final CourseTeachingRepository courseTeachingRepository;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;

  @Transactional
  public CourseView create(CreateCourseRequest request) {
    if (courseRepository.existsByRef(request.ref())) {
      throw new IllegalArgumentException("A course already exists with ref " + request.ref());
    }
    Course saved =
        courseRepository.save(
            Course.builder()
                .ref(request.ref())
                .title(request.title())
                .credits(request.credits())
                .build());
    return toView(saved);
  }

  @Transactional(readOnly = true)
  public List<CourseView> list() {
    return courseRepository.findAll().stream().map(CourseService::toView).toList();
  }

  @Transactional
  public CourseTeachingView assignTeaching(CreateCourseTeachingRequest request) {
    Course course =
        courseRepository
            .findById(request.courseId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Course not found: " + request.courseId()));
    User teacher = findTeacher(request.teacherId());
    Group group =
        groupRepository
            .findById(request.groupId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Group not found: " + request.groupId()));

    if (courseTeachingRepository.existsByCourseAndTeacherAndGroup(course, teacher, group)) {
      throw new IllegalArgumentException(
          "This teacher already teaches " + course.getRef() + " to group " + group.getRef());
    }
    CourseTeaching saved =
        courseTeachingRepository.save(
            CourseTeaching.builder().course(course).teacher(teacher).group(group).build());
    return toView(saved);
  }

  private User findTeacher(UUID teacherId) {
    User teacher =
        userRepository
            .findById(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + teacherId));
    if (teacher.getRole() != Role.TEACHER) {
      throw new IllegalArgumentException("User " + teacher.getEmail() + " is not a teacher");
    }
    return teacher;
  }

  public static CourseView toView(Course course) {
    return new CourseView(course.getId(), course.getRef(), course.getTitle(), course.getCredits());
  }

  public static CourseTeachingView toView(CourseTeaching teaching) {
    return new CourseTeachingView(
        teaching.getId(),
        teaching.getCourse().getRef(),
        teaching.getCourse().getTitle(),
        teaching.getTeacher().getEmail(),
        teaching.getGroup().getRef());
  }
}
