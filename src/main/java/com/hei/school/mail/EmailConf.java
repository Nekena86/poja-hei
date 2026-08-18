package com.hei.school.mail;

import com.hei.school.PojaGenerated;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@PojaGenerated
@Configuration
public class EmailConf {

  @Getter private final String sesSource;
  private final Region region;

  /**
   * In the SES sandbox both the sender and the recipient must be verified identities, so the sender
   * has to be configurable per environment.
   */
  public EmailConf(
      @Value("${aws.ses.source}") String sesSource, @Value("${aws.region}") String region) {
    this.sesSource = sesSource;
    this.region = Region.of(region);
  }

  @Bean
  public SesClient getSesClient() {
    return SesClient.builder().region(region).build();
  }
}
