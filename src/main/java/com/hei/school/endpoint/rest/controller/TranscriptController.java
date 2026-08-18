package com.hei.school.endpoint.rest.controller;

import com.hei.school.endpoint.event.EventProducer;
import com.hei.school.endpoint.event.model.TranscriptPdfRequested;
import com.hei.school.entity.Role;
import com.hei.school.entity.User;
import com.hei.school.exception.ForbiddenOperationException;
import com.hei.school.exception.ResourceNotFoundException;
import com.hei.school.repository.UserRepository;
import com.hei.school.security.CurrentUserProvider;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students/{studentId}/transcript")
@AllArgsConstructor
public class TranscriptController {

  private final UserRepository userRepository;
  private final EventProducer eventProducer;
  private final CurrentUserProvider currentUserProvider;

  @PostMapping("/email")
  public ResponseEntity<Map<String, String>> emailTranscript(
      @PathVariable UUID studentId, @RequestParam String to) {
    User student =
        userRepository
            .findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + studentId));

    User requester = currentUserProvider.getCurrentUser();
    boolean isSelf = requester.getId().equals(student.getId());
    boolean isAdmin = requester.getRole() == Role.ADMIN;
    if (!isSelf && !isAdmin) {
      throw new ForbiddenOperationException("You may only request your own transcript");
    }

    var event = TranscriptPdfRequested.builder().studentId(studentId).recipientEmail(to).build();
    eventProducer.accept(List.of(event));

    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(Map.of("status", "queued", "message", "Your transcript will be emailed shortly"));
  }
}
