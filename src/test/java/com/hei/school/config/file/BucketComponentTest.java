package com.hei.school.config.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hei.school.file.bucket.LocalBucketComponent;
import com.hei.school.file.bucket.S3BucketComponent;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
class BucketComponentTest {

  @Mock private S3Client s3Client;
  @Mock private S3Presigner s3Presigner;

  @Test
  void uploadsToTheConfiguredBucketAndKey(@TempDir Path tmp) throws Exception {
    Path file = Files.writeString(tmp.resolve("transcript.pdf"), "pdf bytes");
    var component = new S3BucketComponent(s3Client, s3Presigner, "hei-transcripts");

    component.upload(file.toFile(), "transcripts/abc.pdf");

    var request = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client).putObject(request.capture(), any(RequestBody.class));
    assertThat(request.getValue().bucket()).isEqualTo("hei-transcripts");
    assertThat(request.getValue().key()).isEqualTo("transcripts/abc.pdf");
    assertThat(component.getBucketName()).isEqualTo("hei-transcripts");
  }

  @Test
  void presignsALinkThatCarriesTheRequestedValidity() throws Exception {
    var presigned = mock(PresignedGetObjectRequest.class);
    when(presigned.url()).thenReturn(URI.create("https://hei.s3.amazonaws.com/abc?sig=x").toURL());
    when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);
    var component = new S3BucketComponent(s3Client, s3Presigner, "hei-transcripts");

    URI link = component.presign("transcripts/abc.pdf", Duration.ofDays(7));

    assertThat(link).hasToString("https://hei.s3.amazonaws.com/abc?sig=x");
    var request = ArgumentCaptor.forClass(GetObjectPresignRequest.class);
    verify(s3Presigner).presignGetObject(request.capture());
    assertThat(request.getValue().signatureDuration()).isEqualTo(Duration.ofDays(7));
  }

  @Test
  void downloadsTheObjectToALocalFile() throws Exception {
    var body =
        new ResponseInputStream<>(
            GetObjectResponse.builder().build(),
            AbortableInputStream.create(
                new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8))));
    when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(body);
    var component = new S3BucketComponent(s3Client, s3Presigner, "hei-transcripts");

    var file = component.download("transcripts/abc.pdf");

    assertThat(Files.readString(file.toPath())).isEqualTo("hello");
  }

  // ---------------------------------------------------------------- local fallback

  @Test
  void theLocalFallbackStoresAndReadsBackTheFile(@TempDir Path root) throws Exception {
    Path source = Files.writeString(root.resolve("source.pdf"), "local bytes");
    var component = new LocalBucketComponent(root.resolve("bucket"), "hei-transcripts");

    component.upload(source.toFile(), "transcripts/abc.pdf");

    assertThat(Files.readString(component.download("transcripts/abc.pdf").toPath()))
        .isEqualTo("local bytes");
    assertThat(component.presign("transcripts/abc.pdf", Duration.ofDays(1)).toString())
        .contains("transcripts/abc.pdf");
    assertThat(component.getBucketName()).isEqualTo("hei-transcripts");
  }

  @Test
  void theLocalFallbackSaysSoWhenTheKeyIsUnknown(@TempDir Path root) {
    var component = new LocalBucketComponent(root, "hei-transcripts");

    assertThatThrownBy(() -> component.download("missing.pdf"))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("missing.pdf");
  }
}
