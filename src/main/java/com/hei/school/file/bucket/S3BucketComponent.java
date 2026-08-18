package com.hei.school.file.bucket;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@AllArgsConstructor
public class S3BucketComponent implements BucketComponent {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  @Getter private final String bucketName;

  @Override
  public void upload(File file, String key) {
    s3Client.putObject(
        PutObjectRequest.builder().bucket(bucketName).key(key).build(), RequestBody.fromFile(file));
  }

  @Override
  public File download(String key) {
    try {
      Path target = Files.createTempFile("download-", "-" + key.replace('/', '-'));
      var response =
          s3Client.getObject(GetObjectRequest.builder().bucket(bucketName).key(key).build());
      try (response) {
        Files.copy(response, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
      }
      return target.toFile();
    } catch (IOException e) {
      throw new RuntimeException("Could not download " + key + " from bucket " + bucketName, e);
    }
  }

  @Override
  public URI presign(String key, Duration validity) {
    var presigned =
        s3Presigner.presignGetObject(
            GetObjectPresignRequest.builder()
                .signatureDuration(validity)
                .getObjectRequest(GetObjectRequest.builder().bucket(bucketName).key(key).build())
                .build());
    return URI.create(presigned.url().toString());
  }
}
