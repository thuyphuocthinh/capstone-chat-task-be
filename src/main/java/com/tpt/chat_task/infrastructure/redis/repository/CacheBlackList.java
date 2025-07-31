package com.tpt.chat_task.infrastructure.redis.repository;

public interface CacheBlackList {
    boolean findAccessToken(String accessToken);
    void addNewAccessToken(String accessToken);
    void clear();
}
