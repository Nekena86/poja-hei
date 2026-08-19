package com.hei.school.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.hei.school.PojaApplication;
import com.hei.school.endpoint.event.EventConsumer;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class EventHandler implements RequestHandler<SQSEvent, Void> {

  private static final ConfigurableApplicationContext CONTEXT =
      new SpringApplicationBuilder(PojaApplication.class).web(WebApplicationType.NONE).run();

  @Override
  public Void handleRequest(SQSEvent event, Context context) {
    var consumer = CONTEXT.getBean(EventConsumer.class);
    for (SQSEvent.SQSMessage message : event.getRecords()) {
      consumer.consume(message.getBody());
    }
    return null;
  }
}
