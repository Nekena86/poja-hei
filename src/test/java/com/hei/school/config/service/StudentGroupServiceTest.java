package com.hei.school.config.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.hei.school.entity.Group;
import com.hei.school.entity.Role;
import com.hei.school.entity.StudentGroupHistory;
import com.hei.school.entity.User;
import com.hei.school.exception.ForbiddenOperationException;
import com.hei.school.repository.GroupRepository;
import com.hei.school.repository.StudentGroupHistoryRepository;
import com.hei.school.service.StudentGroupService;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentGroupServiceTest {

  @Mock private StudentGroupHistoryRepository historyRepository;
  @Mock private GroupRepository groupRepository;

  private StudentGroupService service;

  private User student;
  private User admin;
  private User teacher;
  private Group oldGroup;
  private Group newGroup;

  @BeforeEach
  void setUp() {
    service = new StudentGroupService(historyRepository, groupRepository);
    student =
        User.builder()
            .id(UUID.randomUUID())
            .email("s@school.io")
            .password("x")
            .firstName("A")
            .lastName("B")
            .role(Role.STUDENT)
            .promotionYear(2024)
            .build();
    admin =
        User.builder()
            .id(UUID.randomUUID())
            .email("a@school.io")
            .password("x")
            .firstName("A")
            .lastName("B")
            .role(Role.ADMIN)
            .build();
    teacher =
        User.builder()
            .id(UUID.randomUUID())
            .email("t@school.io")
            .password("x")
            .firstName("A")
            .lastName("B")
            .role(Role.TEACHER)
            .build();
    oldGroup = Group.builder().id(UUID.randomUUID()).ref("L3-A").build();
    newGroup = Group.builder().id(UUID.randomUUID()).ref("L3-B").build();
  }

  @Test
  void onlyAdminCanChangeGroup() {
    assertThatThrownBy(
            () -> service.changeGroup(student, newGroup.getId(), LocalDate.now(), teacher))
        .isInstanceOf(ForbiddenOperationException.class);
  }

  @Test
  void changingGroupClosesThePreviousHistoryRowAndOpensANewOne() {
    var currentHistory =
        StudentGroupHistory.builder()
            .id(UUID.randomUUID())
            .student(student)
            .group(oldGroup)
            .startDate(LocalDate.of(2024, 9, 1))
            .build();
    when(historyRepository.findByStudentAndEndDateIsNull(student))
        .thenReturn(Optional.of(currentHistory));
    when(groupRepository.findById(newGroup.getId())).thenReturn(Optional.of(newGroup));
    when(historyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    LocalDate effectiveDate = LocalDate.of(2025, 2, 1);
    var result = service.changeGroup(student, newGroup.getId(), effectiveDate, admin);

    assertThat(currentHistory.getEndDate()).isEqualTo(effectiveDate);
    assertThat(result.groupId()).isEqualTo(newGroup.getId());
    assertThat(result.groupRef()).isEqualTo("L3-B");
    assertThat(result.startDate()).isEqualTo(effectiveDate);
    assertThat(result.endDate()).isNull();
  }
}
