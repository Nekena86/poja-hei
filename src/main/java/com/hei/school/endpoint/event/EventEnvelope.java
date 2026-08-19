package com.hei.school.endpoint.event;

import com.fasterxml.jackson.databind.JsonNode;

public record EventEnvelope(String type, JsonNode payload) {}
