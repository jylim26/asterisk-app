package com.example.ari.global.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AriInstantDeserializer extends JsonDeserializer<Instant> {

    private static final DateTimeFormatter ARI_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        try {
            return OffsetDateTime.parse(text, ARI_FORMAT).toInstant();
        } catch (DateTimeParseException e) {
            return Instant.parse(text);
        }
    }
}
