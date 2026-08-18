package com.hei.school.endpoint.event;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;

/**
 * Local substitute for SQS: the event is handed to a separate thread, so the HTTP request returns
 * without waiting for the PDF, the upload and the email. Retries and durability are what SQS adds
 * on top in deployed environments.
 */
@AllArgsConstructor
@Slf4j
public class LocalAsyncEventProducer implements EventProducer {

  private final EventConsumer eventConsumer;
  private final TaskExecutor taskExecutor;

  @Override
  public void accept(List<? extends PojaEvent> events) {
    for (PojaEvent event : events) {
      taskExecutor.execute(
          () -> {
            try {
              eventConsumer.consume(event);
            } catch (RuntimeException e) {
              // Nothing is listening on the caller's side any more: log instead of bubbling up.
              log.error("Handling {} failed", event, e);
            }
          });
    }
  }
}
