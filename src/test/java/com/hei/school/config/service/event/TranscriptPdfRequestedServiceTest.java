package com.hei.school.config.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hei.school.endpoint.event.model.TranscriptPdfRequested;
import com.hei.school.entity.Role;
import com.hei.school.entity.User;
import com.hei.school.file.bucket.BucketComponent;
import com.hei.school.mail.Email;
import com.hei.school.mail.Mailer;
import com.hei.school.repository.UserRepository;
import com.hei.school.service.PdfTranscriptService;
import com.hei.school.service.event.TranscriptPdfRequestedService;
import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TranscriptPdfRequestedServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PdfTranscriptService pdfTranscriptService;
  @Mock private BucketComponent bucketComponent;
  @Mock private Mailer mailer;

  private TranscriptPdfRequestedService service;

  private User student;

  @BeforeEach
  void setUp() {
    service =
        new TranscriptPdfRequestedService(
            userRepository, pdfTranscriptService, bucketComponent, mailer);
    student =
        User.builder()
            .id(UUID.randomUUID())
            .email("student@school.io")
            .password("x")
            .firstName("Marie")
            .lastName("Leroy")
            .role(Role.STUDENT)
            .promotionYear(2024)
            .build();
  }

  @Test
  void generatesThePdfUploadsItAndEmailsAPresignedLink() throws Exception {
    when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
    File pdf = File.createTempFile("test-transcript", ".pdf");
    when(pdfTranscriptService.generate(student)).thenReturn(pdf);
    when(bucketComponent.presign(anyString(), any(Duration.class)))
        .thenReturn(URI.create("https://bucket.example/transcript.pdf"));

    var event =
        TranscriptPdfRequested.builder()
            .studentId(student.getId())
            .recipientEmail("marie@perso.io")
            .build();
    service.accept(event);

    verify(bucketComponent).upload(any(File.class), anyString());
    var emailCaptor = org.mockito.ArgumentCaptor.forClass(Email.class);
    verify(mailer).accept(emailCaptor.capture());
    assertThat(emailCaptor.getValue().to().getAddress()).isEqualTo("marie@perso.io");
    assertThat(emailCaptor.getValue().htmlBody()).contains("https://bucket.example/transcript.pdf");
  }
}
