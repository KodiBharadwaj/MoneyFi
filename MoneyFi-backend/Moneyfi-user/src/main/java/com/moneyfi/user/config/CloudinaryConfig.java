package com.moneyfi.user.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;

import static com.moneyfi.user.util.constants.StringUtils.*;

@Configuration
@Profile("local")
public class CloudinaryConfig {

    @Value("${cloudinary.credentials.cloud-name}")
    private String cloudName;
    @Value("${cloudinary.credentials.api-key}")
    private String apiKey;
    @Value("${cloudinary.credentials.api-secret}")
    private String apiSecret;

    /** Cloudinary image storage for profile image **/
    @Bean
    public Cloudinary getCloudinary(){
        Map config = new HashMap();
        config.put(CLOUDINARY_CLOUD_NAME, cloudName);
        config.put(CLOUDINARY_API_KEY, apiKey);
        config.put(CLOUDINARY_API_SECRET, apiSecret);
        config.put(CLOUDINARY_SECURE, true);
        return new Cloudinary(config);
    }
}
