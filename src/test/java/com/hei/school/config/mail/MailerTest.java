package com.hei.school.config.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hei.school.file.zip.FileTyper;
import com.hei.school.mail.Email;
import com.hei.school.mail.EmailConf;
import com.hei.school.mail.Mailer;
import jakarta.mail.internet.InternetAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;

@ExtendWith(MockitoExtension.class)
class MailerTest {

  @Mock private EmailConf emailConf;
  @Mock private SesClient sesClient;

  @Test
  void buildsAMimeMessageAndHandsTheRawBytesToSes() throws Exception {
    when(emailConf.getSesSource()).thenReturn("noreply@hei.school");
    when(emailConf.getSesClient()).thenReturn(sesClient);
    var mailer = new Mailer(emailConf, new FileTyper());

    mailer.accept(
        new Email(
            new InternetAddress("jean.rakotobe@perso.mg"),
            List.of(),
            List.of(),
            "Votre releve de notes",
            "Voici le lien : https://bucket.s3.amazonaws.com/transcripts/abc.pdf",
            List.of()));

    var request = ArgumentCaptor.forClass(SendRawEmailRequest.class);
    verify(sesClient).sendRawEmail(request.capture());
    String raw =
        new String(request.getValue().rawMessage().data().asByteArray(), StandardCharsets.UTF_8);
    assertThat(raw)
        .contains("jean.rakotobe@perso.mg")
        .contains("noreply@hei.school")
        .contains("transcripts/abc.pdf");
  }
}
