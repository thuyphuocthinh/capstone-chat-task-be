package com.tpt.chat_task.infrastructure.redis.utils;

public class RedisSchema {
    public static String getBlacklistKey(String accessToken) {
        return RedisKeyHelper.getKey("blacklist:" + accessToken);
    }

    public static String getBlacklistPrefix() {
        return RedisKeyHelper.getKey("blacklist");
    }
}
