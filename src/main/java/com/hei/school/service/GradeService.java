package com.hei.school.service;

import com.hei.school.entity.Exam;
import com.hei.school.entity.Grade;
import com.hei.school.entity.GradeHistory;
import com.hei.school.entity.Role;
import com.hei.school.entity.User;
import com.hei.school.exception.ForbiddenOperationException;
import com.hei.school.exception.ResourceNotFoundException;
import com.hei.school.repository.ExamRepository;
import com.hei.school.repository.GradeHistoryRepository;
import com.hei.school.repository.GradeRepository;
import com.hei.school.repository.UserRepository;
import com.hei.school.service.dto.CreateGradeRequest;
import com.hei.school.service.dto.GradeHistoryView;
import com.hei.school.service.dto.GradeView;
import com.hei.school.service.dto.UpdateGradeRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class GradeService {

  private final GradeRepository gradeRepository;
  private final GradeHistoryRepository gradeHistoryRepository;
  private final ExamRepository examRepository;
  private final UserRepository userRepository;

  @Transactional
  public GradeView createGrade(CreateGradeRequest request, User requester) {
    if (requester.getRole() == Role.STUDENT) {
      throw new ForbiddenOperationException("Students cannot record grades");
    }
    User student =
        userRepository
            .findById(request.studentId())
            .orElseThrow(
                () -> new ResourceNotFoundException("User not found: " + request.studentId()));
    if (student.getRole() != Role.STUDENT) {
      throw new IllegalArgumentException("Only students can be graded");
    }
    Exam exam =
        examRepository
            .findById(request.examId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Exam not found: " + request.examId()));
    if (requester.getRole() == Role.TEACHER
        && !exam.getCourseTeaching().getTeacher().getId().equals(requester.getId())) {
      throw new ForbiddenOperationException(
          "Teachers can only grade the exams of the courses they teach");
    }
    if (gradeRepository.existsByStudentAndExam(student, exam)) {
      throw new IllegalArgumentException(
          "This student already has a grade for exam "
              + exam.getRef()
              + "; use PUT /api/grades/{id} to correct it");
    }
    Grade saved =
        gradeRepository.save(
            Grade.builder()
                .student(student)
                .exam(exam)
                .value(request.value())
                .lastModifiedAt(Instant.now())
                .lastModifiedBy(requester.getEmail())
                .build());
    return toView(saved);
  }

  @Transactional(readOnly = true)
  public List<GradeView> getGradesForStudent(User student, User requester) {
    if (requester.getRole() == Role.STUDENT && !requester.getId().equals(student.getId())) {
      throw new ForbiddenOperationException("You may only view your own grades");
    }
    var grades = gradeRepository.findByStudent(student);
    if (requester.getRole() == Role.TEACHER) {
      grades =
          grades.stream()
              .filter(
                  g ->
                      g.getExam()
                          .getCourseTeaching()
                          .getTeacher()
                          .getId()
                          .equals(requester.getId()))
              .toList();
    }
    return grades.stream().map(this::toView).toList();
  }

  @Transactional(readOnly = true)
  public List<GradeHistoryView> getHistory(UUID gradeId, User requester) {
    Grade grade = getGradeOrThrow(gradeId);
    assertCanViewGrade(grade, requester);
    return gradeHistoryRepository.findByGradeOrderByChangedAtAsc(grade).stream()
        .map(
            h ->
                new GradeHistoryView(
                    h.getPreviousValue(),
                    h.getNewValue(),
                    h.getReason(),
                    h.getChangedBy(),
                    h.getChangedAt()))
        .toList();
  }

  @Transactional
  public GradeView updateGrade(UUID gradeId, UpdateGradeRequest request, User requester) {
    if (requester.getRole() == Role.STUDENT) {
      throw new ForbiddenOperationException("Students cannot modify grades");
    }
    Grade grade = getGradeOrThrow(gradeId);

    if (requester.getRole() == Role.TEACHER
        && !grade.getExam().getCourseTeaching().getTeacher().getId().equals(requester.getId())) {
      throw new ForbiddenOperationException(
          "Teachers can only modify grades for the courses they teach");
    }

    var previousValue = grade.getValue();
    grade.setValue(request.value());
    grade.setLastModifiedAt(Instant.now());
    grade.setLastModifiedBy(requester.getEmail());
    gradeRepository.save(grade);

    gradeHistoryRepository.save(
        GradeHistory.builder()
            .grade(grade)
            .previousValue(previousValue)
            .newValue(request.value())
            .reason(request.reason())
            .changedBy(requester.getEmail())
            .changedAt(Instant.now())
            .build());

    return toView(grade);
  }

  private void assertCanViewGrade(Grade grade, User requester) {
    boolean isSelf = requester.getId().equals(grade.getStudent().getId());
    boolean isOwningTeacher =
        requester.getRole() == Role.TEACHER
            && grade.getExam().getCourseTeaching().getTeacher().getId().equals(requester.getId());
    boolean isAdmin = requester.getRole() == Role.ADMIN;
    if (!isSelf && !isOwningTeacher && !isAdmin) {
      throw new ForbiddenOperationException("You are not allowed to view this grade's history");
    }
  }

  private Grade getGradeOrThrow(UUID gradeId) {
    return gradeRepository
        .findById(gradeId)
        .orElseThrow(() -> new ResourceNotFoundException("Grade not found: " + gradeId));
  }

  private GradeView toView(Grade grade) {
    return new GradeView(
        grade.getId(),
        grade.getExam().getRef(),
        grade.getExam().getCourseTeaching().getCourse().getTitle(),
        grade.getExam().getCoefficient(),
        grade.getValue(),
        grade.getLastModifiedAt());
  }
}
