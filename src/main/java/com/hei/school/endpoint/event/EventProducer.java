package com.hei.school.endpoint.event;

import java.util.List;
import java.util.function.Consumer;

/**
 * Hands events over to a consumer that runs outside the caller's thread. Deployed environments use
 * {@link SqsEventProducer}; local runs use {@link LocalAsyncEventProducer}. Either way the caller
 * returns before the event has been handled — see {@link EventConf} for how the two are selected.
 */
public interface EventProducer extends Consumer<List<? extends PojaEvent>> {}
