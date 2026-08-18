package com.hei.school.file.bucket;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class BucketConf {

  @Bean
  @ConditionalOnProperty(name = "app.bucket.transport", havingValue = "s3", matchIfMissing = true)
  public S3Client s3Client(@Value("${aws.region}") String region) {
    return S3Client.builder().region(Region.of(region)).build();
  }

  @Bean
  @ConditionalOnProperty(name = "app.bucket.transport", havingValue = "s3", matchIfMissing = true)
  public S3Presigner s3Presigner(@Value("${aws.region}") String region) {
    return S3Presigner.builder().region(Region.of(region)).build();
  }

  @Bean
  @ConditionalOnProperty(name = "app.bucket.transport", havingValue = "s3", matchIfMissing = true)
  public BucketComponent s3BucketComponent(
      S3Client s3Client, S3Presigner s3Presigner, @Value("${aws.s3.bucket}") String bucket) {
    return new S3BucketComponent(s3Client, s3Presigner, bucket);
  }

  @Bean
  @ConditionalOnProperty(name = "app.bucket.transport", havingValue = "local")
  public BucketComponent localBucketComponent(
      @Value("${app.bucket.local-dir:${java.io.tmpdir}/hei-bucket}") String dir,
      @Value("${aws.s3.bucket}") String bucket) {
    return new LocalBucketComponent(Path.of(dir), bucket);
  }
}
