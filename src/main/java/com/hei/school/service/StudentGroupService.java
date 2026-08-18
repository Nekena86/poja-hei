package com.hei.school.service;

import com.hei.school.entity.Group;
import com.hei.school.entity.Role;
import com.hei.school.entity.StudentGroupHistory;
import com.hei.school.entity.User;
import com.hei.school.exception.ForbiddenOperationException;
import com.hei.school.exception.ResourceNotFoundException;
import com.hei.school.repository.GroupRepository;
import com.hei.school.repository.StudentGroupHistoryRepository;
import com.hei.school.service.dto.CreateGroupRequest;
import com.hei.school.service.dto.GroupAssignmentView;
import com.hei.school.service.dto.GroupView;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class StudentGroupService {

  private final StudentGroupHistoryRepository historyRepository;
  private final GroupRepository groupRepository;

  @Transactional
  public GroupView createGroup(CreateGroupRequest request) {
    if (groupRepository.existsByRef(request.ref())) {
      throw new IllegalArgumentException("A group already exists with ref " + request.ref());
    }
    Group saved = groupRepository.save(Group.builder().ref(request.ref()).build());
    return new GroupView(saved.getId(), saved.getRef());
  }

  @Transactional(readOnly = true)
  public List<GroupView> listGroups() {
    return groupRepository.findAll().stream()
            .map(g -> new GroupView(g.getId(), g.getRef()))
            .toList();
  }

  @Transactional
  public GroupAssignmentView changeGroup(
          User student, UUID newGroupId, LocalDate effectiveDate, User requester) {
    if (requester.getRole() != Role.ADMIN) {
      throw new ForbiddenOperationException("Only admins can change a student's group");
    }
    if (student.getRole() != Role.STUDENT) {
      throw new IllegalArgumentException("Only students belong to groups");
    }
    Group newGroup =
            groupRepository
                    .findById(newGroupId)
                    .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + newGroupId));

    historyRepository
            .findByStudentAndEndDateIsNull(student)
            .ifPresent(
                    current -> {
                      current.setEndDate(effectiveDate);
                      historyRepository.save(current);
                    });

    return toView(
            historyRepository.save(
                    StudentGroupHistory.builder()
                            .student(student)
                            .group(newGroup)
                            .startDate(effectiveDate)
                            .build()));
  }

  @Transactional(readOnly = true)
  public List<GroupAssignmentView> getHistory(User student) {
    return historyRepository.findByStudentOrderByStartDateAsc(student).stream()
            .map(StudentGroupService::toView)
            .toList();
  }

  @Transactional(readOnly = true)
  public Group getCurrentGroup(User student) {
    return historyRepository
            .findByStudentAndEndDateIsNull(student)
            .map(StudentGroupHistory::getGroup)
            .orElse(null);
  }

  public static GroupAssignmentView toView(StudentGroupHistory assignment) {
    return new GroupAssignmentView(
            assignment.getId(),
            assignment.getGroup().getId(),
            assignment.getGroup().getRef(),
            assignment.getStartDate(),
            assignment.getEndDate());
  }
}
