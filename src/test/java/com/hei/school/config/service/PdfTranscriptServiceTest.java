package com.hei.school.config.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.hei.school.entity.Role;
import com.hei.school.entity.User;
import com.hei.school.service.PdfTranscriptService;
import com.hei.school.service.TranscriptService;
import com.hei.school.service.dto.TranscriptLine;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PdfTranscriptServiceTest {

  @Mock private TranscriptService transcriptService;

  @Test
  void generatesANonEmptyPdfFile() throws Exception {
    User student =
        User.builder()
            .id(UUID.randomUUID())
            .email("student@school.io")
            .password("x")
            .firstName("Marie")
            .lastName("Leroy")
            .role(Role.STUDENT)
            .promotionYear(2024)
            .build();
    when(transcriptService.getTranscriptLines(student))
        .thenReturn(
            List.of(
                new TranscriptLine(
                    "Algo", "ALG101-CC1", BigDecimal.ONE, BigDecimal.valueOf(15.5))));

    var file = new PdfTranscriptService(transcriptService).generate(student);

    assertThat(file).exists();
    assertThat(file.length()).isGreaterThan(0);
  }
}
