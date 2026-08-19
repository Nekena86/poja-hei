package com.hei.school.endpoint.event;

import java.util.List;
import java.util.function.Consumer;

public interface EventProducer extends Consumer<List<? extends PojaEvent>> {}
