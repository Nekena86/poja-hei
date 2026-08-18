package com.hei.school.endpoint.rest.controller;

import com.hei.school.security.CurrentUserProvider;
import com.hei.school.service.ExamService;
import com.hei.school.service.dto.CreateExamRequest;
import com.hei.school.service.dto.ExamView;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exams")
@AllArgsConstructor
public class ExamController {

  private final ExamService examService;
  private final CurrentUserProvider currentUserProvider;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ExamView create(@Valid @RequestBody CreateExamRequest request) {
    return examService.create(request, currentUserProvider.getCurrentUser());
  }
}
