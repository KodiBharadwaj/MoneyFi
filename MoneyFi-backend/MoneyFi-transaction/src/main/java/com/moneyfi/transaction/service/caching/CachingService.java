package com.moneyfi.transaction.service.caching;

import com.moneyfi.constants.dto.CategoryResponseDto;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

public class CachingService {

    private CachingService() {}

    public static String getCategoryNamesFromCache(Integer categoryId, String type, RedisTemplate redisTemplate) {
        String key = "categoryList::" + type;
        List<CategoryResponseDto> list = (List<CategoryResponseDto>) redisTemplate.opsForValue().get(key);
        if (list != null) {
            return list.stream()
                    .filter(c -> c.getCategoryId().equals(categoryId))
                    .map(CategoryResponseDto::getCategory)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public static List<Integer> getCategoryIdsFromCache(String type, RedisTemplate redisTemplate) {
        String key = "categoryList::" + type;
        List<CategoryResponseDto> list = (List<CategoryResponseDto>) redisTemplate.opsForValue().get(key);
        if (list != null) {
            return list.stream()
                    .map(CategoryResponseDto::getCategoryId)
                    .toList();
        }
        return List.of();
    }
}
