package com.hei.school.endpoint.event;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;

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
              log.error("Handling {} failed", event, e);
            }
          });
    }
  }
}
