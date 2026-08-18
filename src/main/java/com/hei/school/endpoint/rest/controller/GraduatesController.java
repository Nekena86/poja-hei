package com.hei.school.endpoint.rest.controller;

import com.hei.school.service.GraduatesExcelService;
import com.hei.school.service.PromotionResultsService;
import com.hei.school.service.dto.PromotionResults;
import lombok.AllArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promotions")
@AllArgsConstructor
public class GraduatesController {

  private final GraduatesExcelService graduatesExcelService;
  private final PromotionResultsService promotionResultsService;

  @GetMapping("/{year}/results")
  @PreAuthorize("hasRole('ADMIN')")
  public PromotionResults getResults(@PathVariable int year) {
    return promotionResultsService.getResults(year);
  }

  @GetMapping("/{year}/graduates.xlsx")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<byte[]> downloadGraduates(@PathVariable int year) {
    var rows = graduatesExcelService.computeGraduateRows(year);
    byte[] workbook = graduatesExcelService.toWorkbook(rows);

    var disposition =
        ContentDisposition.attachment().filename("diplomes-" + year + ".xlsx").build();

    return ResponseEntity.ok()
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
        .body(workbook);
  }
}
