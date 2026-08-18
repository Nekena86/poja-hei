package com.hei.school.service;

import com.hei.school.entity.User;
import com.hei.school.service.dto.TranscriptLine;
import java.io.File;
import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PdfTranscriptService {

  private final TranscriptService transcriptService;

  public File generate(User student) throws IOException {
    List<TranscriptLine> lines = transcriptService.getTranscriptLines(student);

    File file = File.createTempFile("transcript-" + student.getId(), ".pdf");
    try (PDDocument document = new PDDocument()) {
      PDPage page = new PDPage(PDRectangle.A4);
      document.addPage(page);

      try (PDPageContentStream content = new PDPageContentStream(document, page)) {
        var titleFont = PDType1Font.HELVETICA_BOLD;
        var bodyFont = PDType1Font.HELVETICA;

        float y = 780;
        content.beginText();
        content.setFont(titleFont, 16);
        content.newLineAtOffset(50, y);
        content.showText("Releve de notes");
        content.endText();

        y -= 25;
        content.beginText();
        content.setFont(bodyFont, 11);
        content.newLineAtOffset(50, y);
        content.showText(
                student.getFirstName() + " " + student.getLastName() + " (" + student.getEmail() + ")");
        content.endText();

        y -= 30;
        content.beginText();
        content.setFont(titleFont, 10);
        content.newLineAtOffset(50, y);
        content.showText(
                String.format("%-30s %-12s %-10s %-6s", "Cours", "Examen", "Coeff.", "Note"));
        content.endText();

        for (TranscriptLine line : lines) {
          y -= 18;
          if (y < 60) {
            break;
          }
          content.beginText();
          content.setFont(bodyFont, 10);
          content.newLineAtOffset(50, y);
          content.showText(
                  String.format(
                          "%-30s %-12s %-10s %-6s",
                          truncate(line.courseTitle(), 30),
                          line.examRef(),
                          line.coefficient(),
                          line.value()));
          content.endText();
        }
      }

      document.save(file);
    }
    return file;
  }

  private String truncate(String value, int maxLength) {
    return value.length() <= maxLength ? value : value.substring(0, maxLength - 1) + "...";
  }
}
