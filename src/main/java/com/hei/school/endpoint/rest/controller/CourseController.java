package com.hei.school.endpoint.rest.controller;

import com.hei.school.service.CourseService;
import com.hei.school.service.dto.CourseTeachingView;
import com.hei.school.service.dto.CourseView;
import com.hei.school.service.dto.CreateCourseRequest;
import com.hei.school.service.dto.CreateCourseTeachingRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class CourseController {

  private final CourseService courseService;

  @PostMapping("/courses")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  public CourseView createCourse(@Valid @RequestBody CreateCourseRequest request) {
    return courseService.create(request);
  }

  @GetMapping("/courses")
  public List<CourseView> listCourses() {
    return courseService.list();
  }

  /** Assigns a teacher to teach a course to one group. */
  @PostMapping("/course-teachings")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  public CourseTeachingView assignTeaching(
      @Valid @RequestBody CreateCourseTeachingRequest request) {
    return courseService.assignTeaching(request);
  }
}
