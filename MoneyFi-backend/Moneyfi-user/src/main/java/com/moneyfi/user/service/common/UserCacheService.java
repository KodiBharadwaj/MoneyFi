package com.moneyfi.user.service.common;

import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

@Service
public class UserCacheService {

    @CachePut(value = "userNames", key = "#userId")
    public String updateUserNameCache(Long userId, String newName) {
        return newName;
    }
}
