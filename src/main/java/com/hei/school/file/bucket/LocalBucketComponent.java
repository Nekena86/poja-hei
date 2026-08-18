package com.hei.school.file.bucket;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Development fallback used when no AWS credentials are available: files are written under a local
 * directory and "presigned" links point at that file. The link only works on the machine that
 * generated it — deployed environments must use {@link S3BucketComponent}.
 */
@Slf4j
public class LocalBucketComponent implements BucketComponent {

  private final Path root;
  @Getter private final String bucketName;

  public LocalBucketComponent(Path root, String bucketName) {
    this.root = root;
    this.bucketName = bucketName;
    log.warn(
        "Bucket transport is 'local': files are stored in {} and links are not shareable. "
            + "Set app.bucket.transport=s3 in deployed environments.",
        root);
  }

  @Override
  public void upload(File file, String key) {
    try {
      Path target = root.resolve(key);
      Files.createDirectories(target.getParent());
      Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException("Could not store " + key + " under " + root, e);
    }
  }

  @Override
  public File download(String key) {
    Path target = root.resolve(key);
    if (!Files.exists(target)) {
      throw new RuntimeException("No such key in local bucket: " + key);
    }
    return target.toFile();
  }

  @Override
  public URI presign(String key, Duration validity) {
    return root.resolve(key).toUri();
  }
}
