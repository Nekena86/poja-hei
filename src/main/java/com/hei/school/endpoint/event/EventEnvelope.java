package com.hei.school.endpoint.event;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * What actually travels on the queue: the concrete event class name so the consumer can rebuild the
 * right type, plus the serialized event itself.
 */
public record EventEnvelope(String type, JsonNode payload) {}
