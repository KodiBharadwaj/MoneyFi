package com.moneyfi.apigateway.config;

import com.moneyfi.apigateway.util.enums.UserRoles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    private final UserDetailsService userDetailsService;
    private final JwtFilter jwtFilter;

    public SecurityConfig(UserDetailsService userDetailsService,
                          JwtFilter jwtFilter){
        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));

        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(customizer -> customizer.disable())
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/v1/common/**").hasAnyRole(UserRoles.ADMIN.name(), UserRoles.USER.name(), UserRoles.MAINTAINER.name())
                        .requestMatchers("/api/v1/user/**").hasRole(UserRoles.USER.name())
                        .requestMatchers("/api/v1/gmail-sync/**").hasRole(UserRoles.USER.name())
                        .requestMatchers("/api/v1/maintainer/**").hasRole(UserRoles.MAINTAINER.name())
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/v1/Oauth/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        .requestMatchers("/api/v1/transaction/income/**").hasRole(UserRoles.USER.name())
                        .requestMatchers("/api/v1/transaction/expense/**").hasRole(UserRoles.USER.name())
                        .requestMatchers("/api/v1/transaction/**").hasRole(UserRoles.USER.name())

                        .requestMatchers("/api/v1/wealth-core/budget/**").hasRole(UserRoles.USER.name())
                        .requestMatchers("/api/v1/wealth-core/goal/**").hasRole(UserRoles.USER.name())
                        .requestMatchers("/api/v1/wealth-core/admin/**").hasRole(UserRoles.ADMIN.name())
                        .requestMatchers("/api/v1/wealth-core/common/**").hasAnyRole(UserRoles.ADMIN.name(), UserRoles.USER.name())
                        .requestMatchers("/api/v1/wealth-core/**").hasRole(UserRoles.USER.name())

                        .requestMatchers("/api/v1/user-service/open/**").permitAll()
                        .requestMatchers("/api/v1/user-service/admin/**").hasRole(UserRoles.ADMIN.name())
                        .requestMatchers("/api/v1/user-service/external-api/**").hasRole(UserRoles.USER.name())
                        .requestMatchers("/api/v1/user-service/user/**").hasRole(UserRoles.USER.name())
                        .anyRequest().hasAnyRole(UserRoles.USER.name()))
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(Customizer.withDefaults()); // Ensure cors is enabled in SecurityFilterChain
        return http.build();
    }


    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern(allowedOrigins.trim()); // Allow all origins, you can replace "*" with your client URL
        config.addAllowedHeader("*"); // Allow all headers
        config.addAllowedMethod("*"); // Allow all HTTP methods (GET, POST, etc.)
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    @Bean
    public RestTemplate externalRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RestTemplate externalRestTemplateForOAuth(RestTemplateBuilder builder) {
        return builder.build(); // No @LoadBalanced
    }
}
