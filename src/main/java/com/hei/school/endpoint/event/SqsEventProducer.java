package com.hei.school.endpoint.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@AllArgsConstructor
@Slf4j
public class SqsEventProducer implements EventProducer {

  private final SqsClient sqsClient;
  private final ObjectMapper objectMapper;
  private final String queueUrl;

  @Override
  public void accept(List<? extends PojaEvent> events) {
    for (PojaEvent event : events) {
      var envelope = new EventEnvelope(event.getClass().getName(), objectMapper.valueToTree(event));
      String body;
      try {
        body = objectMapper.writeValueAsString(envelope);
      } catch (Exception e) {
        throw new RuntimeException("Could not serialize event " + event, e);
      }
      sqsClient.sendMessage(
          SendMessageRequest.builder().queueUrl(queueUrl).messageBody(body).build());
      log.info("Published {} on {}", event.getClass().getSimpleName(), queueUrl);
    }
  }
}
