package com.tpt.chat_task.infrastructure.redis.utils;

import java.util.Objects;

public class RedisKeyHelper {
    final private static String defaultPrefix = "chat_task";

    private static String prefix = null;

    public static void setPrefix(String prefix) {
        RedisKeyHelper.prefix = prefix;
    }

    public static String getKey(String key) {
        return getPrefix() + ":" + key;
    }

    public static String getPrefix() {
        return Objects.requireNonNullElse(prefix, defaultPrefix);
    }
}