package com.hei.school.endpoint.rest.controller;

import com.hei.school.entity.User;
import com.hei.school.exception.ResourceNotFoundException;
import com.hei.school.repository.UserRepository;
import com.hei.school.security.CurrentUserProvider;
import com.hei.school.service.StudentGroupService;
import com.hei.school.service.dto.GroupAssignmentView;
import com.hei.school.service.dto.GroupChangeRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students/{studentId}/group")
@AllArgsConstructor
public class StudentGroupController {

  private final StudentGroupService studentGroupService;
  private final UserRepository userRepository;
  private final CurrentUserProvider currentUserProvider;

  @GetMapping("/history")
  public List<GroupAssignmentView> getHistory(@PathVariable UUID studentId) {
    return studentGroupService.getHistory(findUserOrThrow(studentId));
  }

  @PostMapping("/change")
  public GroupAssignmentView changeGroup(
      @PathVariable UUID studentId, @Valid @RequestBody GroupChangeRequest request) {
    User student = findUserOrThrow(studentId);
    return studentGroupService.changeGroup(
        student,
        request.newGroupId(),
        request.effectiveDate(),
        currentUserProvider.getCurrentUser());
  }

  private User findUserOrThrow(UUID id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
  }
}
