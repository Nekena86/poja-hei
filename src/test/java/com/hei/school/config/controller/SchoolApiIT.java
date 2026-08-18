package com.hei.school.config.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Walks the whole API the way a user would: the admin sets the school up, a teacher grades, a
 * student reads their own results. Everything runs against the throwaway PostgreSQL container.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class SchoolApiIT {

  private static final String PASSWORD = "password1";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  /** Seeded by V42_4, so it exists in every environment. */
  private static final String ADMIN = "admin@hei.school";

  private String uniqueSuffix;

  @BeforeEach
  void setUp() {
    uniqueSuffix = Long.toString(System.nanoTime());
  }

  // ------------------------------------------------------------------ helpers

  private JsonNode postAsAdmin(String path, String json) throws Exception {
    var body =
        mockMvc
            .perform(withJson(post(path), json).with(httpBasic(ADMIN, "admin123")))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readTree(body);
  }

  private MockHttpServletRequestBuilder withJson(
      MockHttpServletRequestBuilder builder, String json) {
    return builder.contentType(MediaType.APPLICATION_JSON).content(json);
  }

  private UUID createTeacher() throws Exception {
    var node =
        postAsAdmin(
            "/api/users",
            """
            {"email": "teacher-%s@hei.school", "password": "%s", "firstName": "Tia",
             "lastName": "Teacher", "role": "TEACHER"}
            """
                .formatted(uniqueSuffix, PASSWORD));
    return UUID.fromString(node.get("id").asText());
  }

  private UUID createStudent(int promotionYear, String tag) throws Exception {
    var node =
        postAsAdmin(
            "/api/users",
            """
            {"email": "student-%s-%s@hei.school", "password": "%s", "firstName": "Sam",
             "lastName": "Student", "role": "STUDENT", "promotionYear": %d}
            """
                .formatted(tag, uniqueSuffix, PASSWORD, promotionYear));
    return UUID.fromString(node.get("id").asText());
  }

  private UUID createTeaching(UUID teacherId) throws Exception {
    var group =
        postAsAdmin(
            "/api/groups",
            """
            {"ref": "GRP-%s"}"""
                .formatted(uniqueSuffix));
    var course =
        postAsAdmin(
            "/api/courses",
            """
            {"ref": "CRS-%s", "title": "Systemes distribues", "credits": 5}"""
                .formatted(uniqueSuffix));
    var teaching =
        postAsAdmin(
            "/api/course-teachings",
            """
            {"courseId": "%s", "teacherId": "%s", "groupId": "%s"}"""
                .formatted(course.get("id").asText(), teacherId, group.get("id").asText()));
    return UUID.fromString(teaching.get("id").asText());
  }

  private UUID createExam(UUID teachingId, int academicYear) throws Exception {
    var exam =
        postAsAdmin(
            "/api/exams",
            """
            {"ref": "EX-%s-%d", "courseTeachingId": "%s", "dateExam": "2025-01-20T08:00:00Z",
             "coefficient": 2.0, "academicYear": %d}
            """
                .formatted(uniqueSuffix, academicYear, teachingId, academicYear));
    return UUID.fromString(exam.get("id").asText());
  }

  // ------------------------------------------------------------------ the happy path

  @Test
  void adminSetsUpTheSchoolThenATeacherGradesAndTheStudentReadsTheirGrades() throws Exception {
    UUID teacherId = createTeacher();
    UUID studentId = createStudent(2025, "a");
    UUID teachingId = createTeaching(teacherId);
    UUID examId = createExam(teachingId, 1);
    String teacherEmail = "teacher-" + uniqueSuffix + "@hei.school";
    String studentEmail = "student-a-" + uniqueSuffix + "@hei.school";

    // The owning teacher records the grade.
    var gradeBody =
        mockMvc
            .perform(
                withJson(
                        post("/api/grades"),
                        """
                        {"studentId": "%s", "examId": "%s", "value": 14.5}"""
                            .formatted(studentId, examId))
                    .with(httpBasic(teacherEmail, PASSWORD)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.value").value(14.5))
            .andReturn()
            .getResponse()
            .getContentAsString();
    UUID gradeId = UUID.fromString(objectMapper.readTree(gradeBody).get("id").asText());

    // The student sees their own grade.
    mockMvc
        .perform(
            get("/api/students/{id}/grades", studentId).with(httpBasic(studentEmail, PASSWORD)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].value").value(14.5));

    // The teacher corrects it, and must say why.
    mockMvc
        .perform(
            withJson(
                    put("/api/grades/{id}", gradeId),
                    """
                    {"value": 16.0, "reason": "Erreur de report sur la copie"}""")
                .with(httpBasic(teacherEmail, PASSWORD)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.value").value(16.0));

    // The correction is kept, with its reason.
    mockMvc
        .perform(get("/api/grades/{id}/history", gradeId).with(httpBasic(teacherEmail, PASSWORD)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].previousValue").value(14.5))
        .andExpect(jsonPath("$[0].newValue").value(16.0))
        .andExpect(jsonPath("$[0].reason").value("Erreur de report sur la copie"))
        .andExpect(jsonPath("$[0].changedBy").value(teacherEmail));
  }

  @Test
  void aGradeCannotBeChangedWithoutAReason() throws Exception {
    UUID teacherId = createTeacher();
    UUID studentId = createStudent(2025, "b");
    UUID examId = createExam(createTeaching(teacherId), 1);
    String teacherEmail = "teacher-" + uniqueSuffix + "@hei.school";

    var gradeBody =
        mockMvc
            .perform(
                withJson(
                        post("/api/grades"),
                        """
                        {"studentId": "%s", "examId": "%s", "value": 11}"""
                            .formatted(studentId, examId))
                    .with(httpBasic(teacherEmail, PASSWORD)))
            .andReturn()
            .getResponse()
            .getContentAsString();
    UUID gradeId = UUID.fromString(objectMapper.readTree(gradeBody).get("id").asText());

    mockMvc
        .perform(
            withJson(
                    put("/api/grades/{id}", gradeId),
                    """
                    {"value": 18, "reason": "  "}""")
                .with(httpBasic(teacherEmail, PASSWORD)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void aGradeOutsideZeroToTwentyIsRejected() throws Exception {
    UUID teacherId = createTeacher();
    UUID studentId = createStudent(2025, "c");
    UUID examId = createExam(createTeaching(teacherId), 1);

    mockMvc
        .perform(
            withJson(
                    post("/api/grades"),
                    """
                    {"studentId": "%s", "examId": "%s", "value": 21}"""
                        .formatted(studentId, examId))
                .with(httpBasic(ADMIN, "admin123")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void theSameStudentCannotBeGradedTwiceForTheSameExam() throws Exception {
    UUID teacherId = createTeacher();
    UUID studentId = createStudent(2025, "d");
    UUID examId = createExam(createTeaching(teacherId), 1);
    String json =
        """
        {"studentId": "%s", "examId": "%s", "value": 12}"""
            .formatted(studentId, examId);

    mockMvc
        .perform(withJson(post("/api/grades"), json).with(httpBasic(ADMIN, "admin123")))
        .andExpect(status().isCreated());
    mockMvc
        .perform(withJson(post("/api/grades"), json).with(httpBasic(ADMIN, "admin123")))
        .andExpect(status().isBadRequest());
  }

  // ------------------------------------------------------------------ authorisation

  @Test
  void aTeacherCannotGradeAnExamOfACourseTheyDoNotTeach() throws Exception {
    UUID owningTeacher = createTeacher();
    UUID studentId = createStudent(2025, "e");
    UUID examId = createExam(createTeaching(owningTeacher), 1);

    var outsider =
        postAsAdmin(
            "/api/users",
            """
            {"email": "outsider-%s@hei.school", "password": "%s", "firstName": "Out",
             "lastName": "Sider", "role": "TEACHER"}
            """
                .formatted(uniqueSuffix, PASSWORD));
    assertThat(outsider.get("role").asText()).isEqualTo("TEACHER");

    mockMvc
        .perform(
            withJson(
                    post("/api/grades"),
                    """
                    {"studentId": "%s", "examId": "%s", "value": 12}"""
                        .formatted(studentId, examId))
                .with(httpBasic("outsider-" + uniqueSuffix + "@hei.school", PASSWORD)))
        .andExpect(status().isForbidden());
  }

  @Test
  void aStudentCannotCreateUsers() throws Exception {
    createStudent(2025, "f");
    mockMvc
        .perform(
            withJson(
                    post("/api/users"),
                    """
                    {"email": "sneaky-%s@hei.school", "password": "%s", "firstName": "S",
                     "lastName": "S", "role": "ADMIN"}
                    """
                        .formatted(uniqueSuffix, PASSWORD))
                .with(httpBasic("student-f-" + uniqueSuffix + "@hei.school", PASSWORD)))
        .andExpect(status().isForbidden());
  }

  @Test
  void aStudentCannotReadThePromotionResults() throws Exception {
    createStudent(2025, "g");
    mockMvc
        .perform(
            get("/api/promotions/{year}/results", 2022)
                .with(httpBasic("student-g-" + uniqueSuffix + "@hei.school", PASSWORD)))
        .andExpect(status().isForbidden());
  }

  @Test
  void anUnknownStudentGivesANotFound() throws Exception {
    mockMvc
        .perform(
            get("/api/students/{id}/grades", UUID.randomUUID()).with(httpBasic(ADMIN, "admin123")))
        .andExpect(status().isNotFound());
  }

  // ------------------------------------------------------------------ validation of the model

  @Test
  void aStudentMustBelongToAPromotion() throws Exception {
    mockMvc
        .perform(
            withJson(
                    post("/api/users"),
                    """
                    {"email": "nopromo-%s@hei.school", "password": "%s", "firstName": "N",
                     "lastName": "P", "role": "STUDENT"}
                    """
                        .formatted(uniqueSuffix, PASSWORD))
                .with(httpBasic(ADMIN, "admin123")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void aTeacherCannotBelongToAPromotion() throws Exception {
    mockMvc
        .perform(
            withJson(
                    post("/api/users"),
                    """
                    {"email": "promoteacher-%s@hei.school", "password": "%s", "firstName": "N",
                     "lastName": "P", "role": "TEACHER", "promotionYear": 2024}
                    """
                        .formatted(uniqueSuffix, PASSWORD))
                .with(httpBasic(ADMIN, "admin123")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void anEmailCanOnlyBeUsedOnce() throws Exception {
    createTeacher();
    mockMvc
        .perform(
            withJson(
                    post("/api/users"),
                    """
                    {"email": "teacher-%s@hei.school", "password": "%s", "firstName": "Dup",
                     "lastName": "Licate", "role": "TEACHER"}
                    """
                        .formatted(uniqueSuffix, PASSWORD))
                .with(httpBasic(ADMIN, "admin123")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void anInvalidEmailIsRejected() throws Exception {
    mockMvc
        .perform(
            withJson(
                    post("/api/users"),
                    """
                    {"email": "not-an-email", "password": "%s", "firstName": "N",
                     "lastName": "P", "role": "TEACHER"}
                    """
                        .formatted(PASSWORD))
                .with(httpBasic(ADMIN, "admin123")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void onlyATeacherCanBeAssignedToTeachACourse() throws Exception {
    UUID studentId = createStudent(2025, "h");
    var group =
        postAsAdmin(
            "/api/groups",
            """
            {"ref": "GRP-B-%s"}"""
                .formatted(uniqueSuffix));
    var course =
        postAsAdmin(
            "/api/courses",
            """
            {"ref": "CRS-B-%s", "title": "Reseaux", "credits": 3}"""
                .formatted(uniqueSuffix));

    mockMvc
        .perform(
            withJson(
                    post("/api/course-teachings"),
                    """
                    {"courseId": "%s", "teacherId": "%s", "groupId": "%s"}"""
                        .formatted(course.get("id").asText(), studentId, group.get("id").asText()))
                .with(httpBasic(ADMIN, "admin123")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void aCourseRefIsUnique() throws Exception {
    postAsAdmin(
        "/api/courses",
        """
        {"ref": "CRS-U-%s", "title": "Compilation", "credits": 3}"""
            .formatted(uniqueSuffix));
    mockMvc
        .perform(
            withJson(
                    post("/api/courses"),
                    """
                    {"ref": "CRS-U-%s", "title": "Autre", "credits": 3}"""
                        .formatted(uniqueSuffix))
                .with(httpBasic(ADMIN, "admin123")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void aGroupRefIsUnique() throws Exception {
    postAsAdmin(
        "/api/groups",
        """
        {"ref": "GRP-U-%s"}"""
            .formatted(uniqueSuffix));
    mockMvc
        .perform(
            withJson(
                    post("/api/groups"),
                    """
                    {"ref": "GRP-U-%s"}"""
                        .formatted(uniqueSuffix))
                .with(httpBasic(ADMIN, "admin123")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void theSameTeachingCannotBeAssignedTwice() throws Exception {
    UUID teacherId = createTeacher();
    var group =
        postAsAdmin(
            "/api/groups",
            """
            {"ref": "GRP-T-%s"}"""
                .formatted(uniqueSuffix));
    var course =
        postAsAdmin(
            "/api/courses",
            """
            {"ref": "CRS-T-%s", "title": "Securite", "credits": 3}"""
                .formatted(uniqueSuffix));
    String json =
        """
        {"courseId": "%s", "teacherId": "%s", "groupId": "%s"}"""
            .formatted(course.get("id").asText(), teacherId, group.get("id").asText());

    mockMvc
        .perform(withJson(post("/api/course-teachings"), json).with(httpBasic(ADMIN, "admin123")))
        .andExpect(status().isCreated());
    mockMvc
        .perform(withJson(post("/api/course-teachings"), json).with(httpBasic(ADMIN, "admin123")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void anExamMustBelongToAnExistingTeaching() throws Exception {
    mockMvc
        .perform(
            withJson(
                    post("/api/exams"),
                    """
                    {"ref": "EX-GHOST-%s", "courseTeachingId": "%s",
                     "dateExam": "2025-01-20T08:00:00Z", "coefficient": 1.0, "academicYear": 1}
                    """
                        .formatted(uniqueSuffix, UUID.randomUUID()))
                .with(httpBasic(ADMIN, "admin123")))
        .andExpect(status().isNotFound());
  }

  @Test
  void anExamOutsideTheThreeYearsIsRejected() throws Exception {
    UUID teachingId = createTeaching(createTeacher());
    mockMvc
        .perform(
            withJson(
                    post("/api/exams"),
                    """
                    {"ref": "EX-Y4-%s", "courseTeachingId": "%s",
                     "dateExam": "2025-01-20T08:00:00Z", "coefficient": 1.0, "academicYear": 4}
                    """
                        .formatted(uniqueSuffix, teachingId))
                .with(httpBasic(ADMIN, "admin123")))
        .andExpect(status().isBadRequest());
  }

  // ------------------------------------------------------------------ reading lists

  @Test
  void listsCoursesAndGroups() throws Exception {
    createTeaching(createTeacher());
    mockMvc
        .perform(get("/api/courses").with(httpBasic(ADMIN, "admin123")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].ref").exists());
    mockMvc
        .perform(get("/api/groups").with(httpBasic(ADMIN, "admin123")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].ref").exists());
  }

  @Test
  void listsTheStudentsOfAPromotion() throws Exception {
    mockMvc
        .perform(
            get("/api/users").param("promotionYear", "2022").with(httpBasic(ADMIN, "admin123")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].std").exists())
        .andExpect(jsonPath("$[0].role").value("STUDENT"));
  }
}
