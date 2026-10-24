//package com.moneyfi.user.config.caffeine.cache;
//
//import com.github.benmanes.caffeine.cache.Caffeine;
//import org.springframework.cache.caffeine.CaffeineCacheManager;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.concurrent.TimeUnit;
//
//@Configuration
//public class CacheConfig {
//
//    @Bean
//    public CaffeineCacheManager cacheManager() {
//        CaffeineCacheManager cacheManager = new CaffeineCacheManager("userNames");
//        cacheManager.setCaffeine(
//                Caffeine.newBuilder()
//                        .expireAfterWrite(60, TimeUnit.MINUTES)
//                        .maximumSize(1000)
//        );
//        return cacheManager;
//    }
//}
