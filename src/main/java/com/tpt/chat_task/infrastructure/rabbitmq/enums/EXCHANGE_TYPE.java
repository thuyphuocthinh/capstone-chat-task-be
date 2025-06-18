package com.tpt.chat_task.infrastructure.rabbitmq.enums;

public enum EXCHANGE_TYPE {
    DIRECT("direct"),
    FANOUT("fanout"),
    TOPIC("topic"),
    HEADERS("headers");

    private final String value;

    EXCHANGE_TYPE(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
