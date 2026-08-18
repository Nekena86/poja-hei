package com.hei.school.endpoint.rest.controller;

import com.hei.school.service.StudentGroupService;
import com.hei.school.service.dto.CreateGroupRequest;
import com.hei.school.service.dto.GroupView;
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
@RequestMapping("/api/groups")
@AllArgsConstructor
public class GroupController {

  private final StudentGroupService studentGroupService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  public GroupView create(@Valid @RequestBody CreateGroupRequest request) {
    return studentGroupService.createGroup(request);
  }

  @GetMapping
  public List<GroupView> list() {
    return studentGroupService.listGroups();
  }
}
