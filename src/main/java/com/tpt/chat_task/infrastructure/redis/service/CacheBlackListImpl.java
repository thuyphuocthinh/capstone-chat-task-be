package com.tpt.chat_task.infrastructure.redis.service;

import com.tpt.chat_task.infrastructure.redis.repository.CacheBlackList;
import com.tpt.chat_task.infrastructure.redis.utils.RedisSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
@RequiredArgsConstructor
public class CacheBlackListImpl implements CacheBlackList {
    private final JedisPool jedisPool;

    private static final int DEFAULT_TTL_SECONDS = 3600; // 1 hour

    @Override
    public boolean findAccessToken(String accessToken) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = RedisSchema.getBlacklistKey(accessToken);
            return jedis.exists(key);
        }
    }

    @Override
    public void addNewAccessToken(String accessToken) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = RedisSchema.getBlacklistKey(accessToken);
            jedis.setex(key, DEFAULT_TTL_SECONDS, "blacklisted");
        }
    }

    @Override
    public void clear() {
        try (Jedis jedis = jedisPool.getResource()) {
            var keys = jedis.keys(RedisSchema.getBlacklistPrefix() + "*");
            if (keys != null && !keys.isEmpty()) {
                jedis.del(keys.toArray(new String[0]));
            }
        }
    }
}
