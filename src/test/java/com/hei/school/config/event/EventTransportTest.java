package com.hei.school.config.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hei.school.endpoint.event.EventConsumer;
import com.hei.school.endpoint.event.LocalAsyncEventProducer;
import com.hei.school.endpoint.event.SqsEventProducer;
import com.hei.school.endpoint.event.model.TranscriptPdfRequested;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.SyncTaskExecutor;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/** The two ways an event leaves the HTTP thread: a real queue, or a local worker thread. */
@ExtendWith(MockitoExtension.class)
class EventTransportTest {

  private static final String QUEUE = "https://sqs.eu-west-3.amazonaws.com/1/hei-events";

  @Mock private SqsClient sqsClient;
  @Mock private ApplicationContext applicationContext;

  private ObjectMapper objectMapper;
  private TranscriptPdfRequested event;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    event =
        TranscriptPdfRequested.builder()
            .studentId(UUID.fromString("f0000000-0000-0000-0000-000000000001"))
            .recipientEmail("jean@perso.mg")
            .build();
  }

  @Test
  void theSqsProducerSendsAnEnvelopeCarryingTheEventType() {
    new SqsEventProducer(sqsClient, objectMapper, QUEUE).accept(List.of(event));

    var request = ArgumentCaptor.forClass(SendMessageRequest.class);
    verify(sqsClient).sendMessage(request.capture());
    assertThat(request.getValue().queueUrl()).isEqualTo(QUEUE);
    assertThat(request.getValue().messageBody())
        .contains(TranscriptPdfRequested.class.getName())
        .contains("f0000000-0000-0000-0000-000000000001")
        .contains("jean@perso.mg");
  }

  @Test
  void theSqsProducerSendsOneMessagePerEvent() {
    new SqsEventProducer(sqsClient, objectMapper, QUEUE).accept(List.of(event, event));

    verify(sqsClient, org.mockito.Mockito.times(2)).sendMessage(any(SendMessageRequest.class));
  }

  @Test
  void theConsumerRebuildsTheEventFromItsMessageAndCallsTheMatchingBean() throws Exception {
    List<TranscriptPdfRequested> handled = new ArrayList<>();
    Consumer<TranscriptPdfRequested> handler = handled::add;
    when(applicationContext.getBean("transcriptPdfRequestedService")).thenReturn(handler);

    new SqsEventProducer(sqsClient, objectMapper, QUEUE).accept(List.of(event));
    var request = ArgumentCaptor.forClass(SendMessageRequest.class);
    verify(sqsClient).sendMessage(request.capture());

    new EventConsumer(applicationContext, objectMapper).consume(request.getValue().messageBody());

    assertThat(handled).hasSize(1);
    assertThat(handled.get(0).getRecipientEmail()).isEqualTo("jean@perso.mg");
  }

  @Test
  void anUnreadableMessageIsRejectedClearly() {
    var consumer = new EventConsumer(applicationContext, objectMapper);

    assertThatThrownBy(() -> consumer.consume("not json"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Not a readable event message");
  }

  @Test
  void anUnknownEventTypeIsRejected() {
    var consumer = new EventConsumer(applicationContext, objectMapper);

    assertThatThrownBy(() -> consumer.consume("{\"type\":\"com.hei.school.Ghost\",\"payload\":{}}"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unknown event type");
  }

  @Test
  void aTypeThatIsNotAnEventIsRejected() {
    var consumer = new EventConsumer(applicationContext, objectMapper);

    assertThatThrownBy(() -> consumer.consume("{\"type\":\"java.lang.String\",\"payload\":{}}"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Not an event type");
  }

  @Test
  void theLocalProducerHandsTheEventToTheConsumer() {
    List<TranscriptPdfRequested> handled = new ArrayList<>();
    Consumer<TranscriptPdfRequested> handler = handled::add;
    when(applicationContext.getBean("transcriptPdfRequestedService")).thenReturn(handler);
    var producer =
        new LocalAsyncEventProducer(
            new EventConsumer(applicationContext, objectMapper), new SyncTaskExecutor());

    producer.accept(List.of(event));

    assertThat(handled).containsExactly(event);
  }

  @Test
  void aFailingHandlerDoesNotBubbleBackToTheCaller() {
    Consumer<TranscriptPdfRequested> exploding =
        e -> {
          throw new IllegalStateException("SES is down");
        };
    when(applicationContext.getBean("transcriptPdfRequestedService")).thenReturn(exploding);
    var producer =
        new LocalAsyncEventProducer(
            new EventConsumer(applicationContext, objectMapper), new SyncTaskExecutor());

    // Nothing is waiting on the result any more, so the failure is logged, not thrown.
    producer.accept(List.of(event));

    verify(sqsClient, never()).sendMessage(any(SendMessageRequest.class));
  }

  @Test
  void theEventCarriesItsOwnRetryBudget() {
    assertThat(event.maxConsumerDuration()).isEqualTo(Duration.ofSeconds(60));
    assertThat(event.maxConsumerBackoffBetweenRetries()).isEqualTo(Duration.ofSeconds(30));
  }
}
