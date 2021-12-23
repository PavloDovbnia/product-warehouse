package com.rost.productwarehouse.itemproperty;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class ItemPropertyValueJsonDeserializer extends StdDeserializer<ItemPropertyValue<?>> {

    public ItemPropertyValueJsonDeserializer() {
        this(null);
    }

    public ItemPropertyValueJsonDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ItemPropertyValue<Object> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        String value = node.get("value").asText();
        return new SingleItemPropertyValue<>(value);
    }
}
