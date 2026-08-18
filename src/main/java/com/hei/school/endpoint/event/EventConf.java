package com.hei.school.endpoint.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * Picks the event transport from {@code app.events.transport}: {@code sqs} (the default, used in
 * deployed environments) or {@code local}.
 */
@Configuration
public class EventConf {

  @Bean
  @ConditionalOnProperty(name = "app.events.transport", havingValue = "sqs", matchIfMissing = true)
  public SqsClient sqsClient(@Value("${aws.region}") String region) {
    return SqsClient.builder().region(Region.of(region)).build();
  }

  @Bean
  @ConditionalOnProperty(name = "app.events.transport", havingValue = "sqs", matchIfMissing = true)
  public EventProducer sqsEventProducer(
      SqsClient sqsClient,
      ObjectMapper objectMapper,
      @Value("${aws.sqs.queue-url}") String queueUrl) {
    return new SqsEventProducer(sqsClient, objectMapper, queueUrl);
  }

  @Bean("eventTaskExecutor")
  @ConditionalOnProperty(name = "app.events.transport", havingValue = "local")
  public TaskExecutor eventTaskExecutor() {
    var executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("event-");
    executor.initialize();
    return executor;
  }

  @Bean
  @ConditionalOnProperty(name = "app.events.transport", havingValue = "local")
  public EventProducer localAsyncEventProducer(
      EventConsumer eventConsumer, @Qualifier("eventTaskExecutor") TaskExecutor taskExecutor) {
    return new LocalAsyncEventProducer(eventConsumer, taskExecutor);
  }
}
