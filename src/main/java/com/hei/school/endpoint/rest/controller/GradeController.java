package com.hei.school.endpoint.rest.controller;

import com.hei.school.entity.User;
import com.hei.school.exception.ResourceNotFoundException;
import com.hei.school.repository.UserRepository;
import com.hei.school.security.CurrentUserProvider;
import com.hei.school.service.GradeService;
import com.hei.school.service.dto.CreateGradeRequest;
import com.hei.school.service.dto.GradeHistoryView;
import com.hei.school.service.dto.GradeView;
import com.hei.school.service.dto.UpdateGradeRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class GradeController {

  private final GradeService gradeService;
  private final UserRepository userRepository;
  private final CurrentUserProvider currentUserProvider;

  @PostMapping("/grades")
  @ResponseStatus(HttpStatus.CREATED)
  public GradeView createGrade(@Valid @RequestBody CreateGradeRequest request) {
    return gradeService.createGrade(request, currentUserProvider.getCurrentUser());
  }

  @GetMapping("/students/{studentId}/grades")
  public List<GradeView> getGradesForStudent(@PathVariable UUID studentId) {
    User student = findUserOrThrow(studentId);
    return gradeService.getGradesForStudent(student, currentUserProvider.getCurrentUser());
  }

  @GetMapping("/grades/{gradeId}/history")
  public List<GradeHistoryView> getHistory(@PathVariable UUID gradeId) {
    return gradeService.getHistory(gradeId, currentUserProvider.getCurrentUser());
  }

  @PutMapping("/grades/{gradeId}")
  public GradeView updateGrade(
      @PathVariable UUID gradeId, @Valid @RequestBody UpdateGradeRequest request) {
    return gradeService.updateGrade(gradeId, request, currentUserProvider.getCurrentUser());
  }

  private User findUserOrThrow(UUID id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
  }
}
