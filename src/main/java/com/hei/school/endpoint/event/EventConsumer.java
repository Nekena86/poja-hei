package com.hei.school.endpoint.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Routes an event to its handler, following the POJA convention: the event {@code Foo} is handled
 * by the bean named {@code fooService}.
 */
@Component
@AllArgsConstructor
@Slf4j
public class EventConsumer {

  private final ApplicationContext applicationContext;
  private final ObjectMapper objectMapper;

  /** Rebuilds the event from a queue message, then dispatches it. */
  public void consume(String messageBody) {
    EventEnvelope envelope;
    try {
      envelope = objectMapper.readValue(messageBody, EventEnvelope.class);
    } catch (Exception e) {
      throw new IllegalArgumentException("Not a readable event message: " + messageBody, e);
    }
    Class<?> eventType;
    try {
      eventType = Class.forName(envelope.type());
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Unknown event type: " + envelope.type(), e);
    }
    if (!PojaEvent.class.isAssignableFrom(eventType)) {
      throw new IllegalArgumentException("Not an event type: " + envelope.type());
    }
    consume((PojaEvent) objectMapper.convertValue(envelope.payload(), eventType));
  }

  @SuppressWarnings("unchecked")
  public void consume(PojaEvent event) {
    String simpleName = event.getClass().getSimpleName();
    String beanName =
        Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1) + "Service";
    log.info("Consuming {} through bean {}", simpleName, beanName);
    Consumer<PojaEvent> consumer = (Consumer<PojaEvent>) applicationContext.getBean(beanName);
    consumer.accept(event);
  }
}
