package project.commonutils.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class CentralCacheConfig {
    public CacheManager globalCacheManager(RedissonConnectionFactory redissonConnectionFactory) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues();
        return RedisCacheManager.builder(redissonConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }

    public CacheManager localCacheManager() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(
                Caffeine.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS) // delete cache
                        .maximumSize(1000) ///  Max 1000 records
        );
        return caffeineCacheManager;
    }
}
