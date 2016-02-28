package com.appsflyer.meetup.aggregator;

import org.apache.flink.api.common.functions.MapFunction;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

public class Event {
    public String appId;
    public String ip;
    public String deviceType;
    public String country;
    public String city;

    public static class Parser implements MapFunction<String, Event> {
        private transient ObjectMapper objectMapper;

        private ObjectMapper getObjectMapper() {
            if (objectMapper == null) {
                objectMapper = new ObjectMapper();
            }
            return objectMapper;
        }

        @Override
        public Event map(String value) throws Exception {
            try {
                return getObjectMapper().readValue(value, Event.class);
            } catch (JsonParseException e) {
                return null;
            }
        }
    }
}
