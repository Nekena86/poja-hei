package com.hei.school.config.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class PromotionApiIT {

  private static final String ADMIN = "admin@hei.school";
  private static final String ADMIN_PASSWORD = "admin123";
  private static final String TEACHER = "rakoto@hei.school";
  private static final String TEACHER_PASSWORD = "teacher123";
  private static final String TOP_STUDENT = "jean.rakotobe@hei.school";
  private static final String FAILED_STUDENT = "paul.randria@hei.school";
  private static final String STUDENT_PASSWORD = "student123";

  @Autowired private MockMvc mockMvc;

  @Test
  void theAdminSeesThePromotionResultsYearByYear() throws Exception {
    mockMvc
        .perform(get("/api/promotions/{year}/results", 2022).with(httpBasic(ADMIN, ADMIN_PASSWORD)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.promotionYear").value(2022))
        .andExpect(jsonPath("$.studentCount").value(6))
        .andExpect(jsonPath("$.graduateCount").value(4))
        // Ranked by overall average, so Jean Rakotobe (15.00) comes first.
        .andExpect(jsonPath("$.students[0].nom").value("Rakotobe"))
        .andExpect(jsonPath("$.students[0].overallAverage").value(15.00))
        .andExpect(jsonPath("$.students[0].graduated").value(true))
        .andExpect(jsonPath("$.students[0].perAcademicYear.length()").value(3))
        .andExpect(jsonPath("$.students[0].perAcademicYear[0].academicYear").value(1))
        .andExpect(jsonPath("$.students[0].perAcademicYear[0].average").value(16.00))
        .andExpect(jsonPath("$.students[0].perAcademicYear[1].average").value(15.00))
        .andExpect(jsonPath("$.students[0].perAcademicYear[2].average").value(14.00));
  }

  @Test
  void theExcelFileListsOnlyTheGraduatesRankedByAverage() throws Exception {
    byte[] xlsx =
        mockMvc
            .perform(
                get("/api/promotions/{year}/graduates.xlsx", 2022)
                    .with(httpBasic(ADMIN, ADMIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(
                content()
                    .contentTypeCompatibleWith(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

    List<String> names = new ArrayList<>();
    try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(xlsx))) {
      var sheet = workbook.getSheetAt(0);
      assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("rang");
      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        names.add(sheet.getRow(i).getCell(2).getStringCellValue());
      }
    }

    assertThat(names).containsExactly("Rakotobe", "Rasoa", "Andria", "Ravelo");
  }

  @Test
  void aTeacherCanAlsoDownloadTheGraduatesFile() throws Exception {
    mockMvc
        .perform(
            get("/api/promotions/{year}/graduates.xlsx", 2022)
                .with(httpBasic(TEACHER, TEACHER_PASSWORD)))
        .andExpect(status().isOk());
  }

  @Test
  void aStudentCannotDownloadTheGraduatesFile() throws Exception {
    mockMvc
        .perform(
            get("/api/promotions/{year}/graduates.xlsx", 2022)
                .with(httpBasic(TOP_STUDENT, STUDENT_PASSWORD)))
        .andExpect(status().isForbidden());
  }

  @Test
  void anUnauthenticatedVisitorGetsAReadableRefusalRatherThanABlankPage() throws Exception {
    var response =
        mockMvc
            .perform(get("/").header("Accept", MediaType.TEXT_HTML_VALUE))
            .andExpect(status().isUnauthorized())
            .andReturn()
            .getResponse();
    assertThat(response.getHeader("WWW-Authenticate")).contains("Basic");
  }

  @Test
  void theRootOpensThePromotionsPage() throws Exception {
    mockMvc
        .perform(get("/").with(httpBasic(ADMIN, ADMIN_PASSWORD)))
        .andExpect(status().is3xxRedirection())
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl(
                "/promotions"));
  }

  @Test
  void thePromotionsPageListsEveryPromotionWithADownloadLink() throws Exception {
    mockMvc
        .perform(get("/promotions").with(httpBasic(ADMIN, ADMIN_PASSWORD)))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("2022")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("2024")))
        .andExpect(
            content()
                .string(
                    org.hamcrest.Matchers.containsString("/api/promotions/2022/graduates.xlsx")))
        .andExpect(
            content().string(org.hamcrest.Matchers.containsString("<button type=\"submit\"")));
  }


  @Test
  void aStudentSeesTheirOwnGradesButNotAnotherStudentsGrades() throws Exception {
    String ownGradesPath = "/api/students/{id}/grades";
    var studentId = idOf(TOP_STUDENT);
    var otherId = idOf(FAILED_STUDENT);

    mockMvc
        .perform(get(ownGradesPath, studentId).with(httpBasic(TOP_STUDENT, STUDENT_PASSWORD)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3));
    mockMvc
        .perform(get(ownGradesPath, otherId).with(httpBasic(TOP_STUDENT, STUDENT_PASSWORD)))
        .andExpect(status().isForbidden());
  }

  @Test
  void theCorrectionMadeAfterAComplaintIsVisibleInTheHistory() throws Exception {
    mockMvc
        .perform(
            get("/api/grades/{id}/history", "20000000-0000-0000-0000-000000000007")
                .with(httpBasic(ADMIN, ADMIN_PASSWORD)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].previousValue").value(6.00))
        .andExpect(jsonPath("$[0].newValue").value(8.00))
        .andExpect(
            jsonPath("$[0].reason").value(org.hamcrest.Matchers.containsString("Reclamation")));
  }


  @Test
  void aStudentsGroupPathIsReadableFromStartToFinish() throws Exception {
    mockMvc
        .perform(
            get("/api/students/{id}/group/history", idOf(FAILED_STUDENT))
                .with(httpBasic(ADMIN, ADMIN_PASSWORD)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].groupRef").value("G-2022-A"))
        .andExpect(jsonPath("$[0].endDate").value("2024-01-15"))
        .andExpect(jsonPath("$[1].groupRef").value("G-2022-B"))
        .andExpect(jsonPath("$[1].endDate").doesNotExist());
  }

  @Test
  void theAdminMovesAStudentToAnotherGroupAndTheOldRowIsClosed() throws Exception {
    var studentId = idOf(TOP_STUDENT);
    mockMvc
        .perform(
            post("/api/students/{id}/group/change", studentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"newGroupId": "c0000000-0000-0000-0000-000000000002",
                     "effectiveDate": "2025-03-01"}""")
                .with(httpBasic(ADMIN, ADMIN_PASSWORD)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.groupRef").value("G-2022-B"))
        .andExpect(jsonPath("$.startDate").value("2025-03-01"));

    mockMvc
        .perform(
            get("/api/students/{id}/group/history", studentId)
                .with(httpBasic(ADMIN, ADMIN_PASSWORD)))
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].endDate").value("2025-03-01"));
  }

  @Test
  void onlyTheAdminMovesAStudentBetweenGroups() throws Exception {
    mockMvc
        .perform(
            post("/api/students/{id}/group/change", idOf(TOP_STUDENT))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"newGroupId": "c0000000-0000-0000-0000-000000000002",
                     "effectiveDate": "2025-03-01"}""")
                .with(httpBasic(TEACHER, TEACHER_PASSWORD)))
        .andExpect(status().isForbidden());
  }

  @Test
  void movingAStudentToAnUnknownGroupGivesANotFound() throws Exception {
    mockMvc
        .perform(
            post("/api/students/{id}/group/change", idOf(TOP_STUDENT))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"newGroupId": "c0000000-0000-0000-0000-0000000000ff",
                     "effectiveDate": "2025-03-01"}""")
                .with(httpBasic(ADMIN, ADMIN_PASSWORD)))
        .andExpect(status().isNotFound());
  }


  @Test
  void aStudentCanAskForTheirOwnTranscriptAndTheRequestReturnsImmediately() throws Exception {
    mockMvc
        .perform(
            post("/api/students/{id}/transcript/email", idOf(TOP_STUDENT))
                .param("to", "jean.rakotobe@perso.mg")
                .with(httpBasic(TOP_STUDENT, STUDENT_PASSWORD)))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.status").value("queued"));
  }

  @Test
  void aStudentCannotAskForAnotherStudentsTranscript() throws Exception {
    mockMvc
        .perform(
            post("/api/students/{id}/transcript/email", idOf(FAILED_STUDENT))
                .param("to", "somewhere@perso.mg")
                .with(httpBasic(TOP_STUDENT, STUDENT_PASSWORD)))
        .andExpect(status().isForbidden());
  }

  @Test
  void askingForTheTranscriptOfAnUnknownStudentGivesANotFound() throws Exception {
    mockMvc
        .perform(
            post("/api/students/{id}/transcript/email", java.util.UUID.randomUUID())
                .param("to", "somewhere@perso.mg")
                .with(httpBasic(ADMIN, ADMIN_PASSWORD)))
        .andExpect(status().isNotFound());
  }


  private String idOf(String email) throws Exception {
    return switch (email) {
      case TOP_STUDENT -> "f0000000-0000-0000-0000-000000000001";
      case FAILED_STUDENT -> "f0000000-0000-0000-0000-000000000003";
      default -> throw new IllegalArgumentException("Unknown seeded user: " + email);
    };
  }
}
