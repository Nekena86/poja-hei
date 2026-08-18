package com.hei.school.service.event;

import com.hei.school.endpoint.event.model.TranscriptPdfRequested;
import com.hei.school.entity.User;
import com.hei.school.exception.ResourceNotFoundException;
import com.hei.school.file.bucket.BucketComponent;
import com.hei.school.mail.Email;
import com.hei.school.mail.Mailer;
import com.hei.school.repository.UserRepository;
import com.hei.school.service.PdfTranscriptService;
import jakarta.mail.internet.InternetAddress;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TranscriptPdfRequestedService implements Consumer<TranscriptPdfRequested> {

  private final UserRepository userRepository;
  private final PdfTranscriptService pdfTranscriptService;
  private final BucketComponent bucketComponent;
  private final Mailer mailer;

  @SneakyThrows
  @Override
  public void accept(TranscriptPdfRequested event) {
    User student =
        userRepository
            .findById(event.getStudentId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Student not found: " + event.getStudentId()));

    var pdfFile = pdfTranscriptService.generate(student);
    String bucketKey = "transcripts/" + student.getId() + "-" + System.currentTimeMillis() + ".pdf";
    bucketComponent.upload(pdfFile, bucketKey);
    var downloadLink = bucketComponent.presign(bucketKey, Duration.ofDays(7));

    mailer.accept(
        new Email(
            new InternetAddress(event.getRecipientEmail()),
            List.of(),
            List.of(),
            "Votre releve de notes",
            "Bonjour "
                + student.getFirstName()
                + ",\n\nVoici le lien pour telecharger votre releve de notes (valable 7 jours) :\n"
                + downloadLink
                + "\n\nCordialement.",
            List.of()));
  }
}
