package com.hei.school.endpoint.event;

import java.time.Duration;

public abstract class PojaEvent {

  public abstract Duration maxConsumerDuration();

  public abstract Duration maxConsumerBackoffBetweenRetries();
}
