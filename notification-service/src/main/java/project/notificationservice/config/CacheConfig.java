package project.notificationservice.config;

import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import project.commonutils.config.CentralCacheConfig;

@Configuration
public class CacheConfig extends CentralCacheConfig {

    @Bean
    public CacheManager cacheManager(RedissonConnectionFactory factory) {
        return super.globalCacheManager(factory);
    }
}
