package com.moneyfi.user.service.general.caching;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

@Service
public class UserCacheService {

    @CachePut(value = "userNames", key = "#userId")
    public String updateUserNameCache(Long userId, String newName) {
        return newName;
    }

    @CacheEvict(value = "userNames", key = "#userId")
    public Long removeUserNameFromRedisCache(Long userId) {
        return userId;
    }

    @CacheEvict(value = "UserProfileDetails", key = "#username")
    public String removeUserProfileDetailsFromRedisCache(String username) {
        return username;
    }
}
