package com.hei.school.file.bucket;

import java.io.File;
import java.net.URI;
import java.time.Duration;

/**
 * File storage for generated documents (transcripts). The deployed environments use {@link
 * S3BucketComponent}; local runs without AWS credentials can fall back to {@link
 * LocalBucketComponent} by setting {@code app.bucket.transport=local}.
 */
public interface BucketComponent {

  void upload(File file, String key);

  File download(String key);

  /** Returns a time-limited link that the recipient can open without credentials. */
  URI presign(String key, Duration validity);

  String getBucketName();
}
