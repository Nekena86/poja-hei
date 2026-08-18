package com.hei.school.service;

import com.hei.school.entity.CourseTeaching;
import com.hei.school.entity.Exam;
import com.hei.school.entity.Role;
import com.hei.school.entity.User;
import com.hei.school.exception.ForbiddenOperationException;
import com.hei.school.exception.ResourceNotFoundException;
import com.hei.school.repository.CourseTeachingRepository;
import com.hei.school.repository.ExamRepository;
import com.hei.school.service.dto.CreateExamRequest;
import com.hei.school.service.dto.ExamView;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final CourseTeachingRepository courseTeachingRepository;

    @Transactional
    public ExamView create(CreateExamRequest request, User requester) {
        CourseTeaching teaching =
                courseTeachingRepository
                        .findById(request.courseTeachingId())
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Course teaching not found: " + request.courseTeachingId()));

        boolean isOwningTeacher =
                requester.getRole() == Role.TEACHER
                        && teaching.getTeacher().getId().equals(requester.getId());
        if (requester.getRole() != Role.ADMIN && !isOwningTeacher) {
            throw new ForbiddenOperationException(
                    "Only an admin or the teacher of this course can create its exams");
        }
        if (examRepository.existsByRef(request.ref())) {
            throw new IllegalArgumentException("An exam already exists with ref " + request.ref());
        }

        Exam saved =
                examRepository.save(
                        Exam.builder()
                                .ref(request.ref())
                                .courseTeaching(teaching)
                                .dateExam(request.dateExam())
                                .coefficient(request.coefficient())
                                .academicYear(request.academicYear())
                                .build());
        return toView(saved);
    }

    public static ExamView toView(Exam exam) {
        return new ExamView(
                exam.getId(),
                exam.getRef(),
                exam.getCourseTeaching().getCourse().getTitle(),
                exam.getDateExam(),
                exam.getCoefficient(),
                exam.getAcademicYear());
    }
}
