package com.moneyfi.user.service.general.caching;

import com.moneyfi.constants.dto.CategoryResponseDto;
import com.moneyfi.user.service.user.dto.response.ProfileDetailsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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

    public List<String> getCategoryFromCache(String type) {
        String key = "categoryList::" + type;
        List<CategoryResponseDto> list = (List<CategoryResponseDto>) redisTemplate.opsForValue().get(key);
        if (list != null) {
            return list.stream()
                    .map(response -> (response.getCategory() + "-" + response.getCategoryId()))
                    .toList();
        }
        return List.of();
    }

    @CachePut(value = "UserProfileDetails", key = "#username")
    public ProfileDetailsDto updateUserProfileDetails(String username, ProfileDetailsDto profileDetailsDto) {
        return profileDetailsDto;
    }
}
