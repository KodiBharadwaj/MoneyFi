package com.moneyfi.wealthcore.service.webclient;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Service
public class WebClientService {

    private final WebClient webClient;

    public WebClientService(WebClient webClient) {
        this.webClient = webClient;
    }

    public <T, R> R exchange(HttpMethod method, String url, Map<String, String> queryParams, T body, String token, ParameterizedTypeReference<R> responseType) {
        WebClient.RequestBodySpec request = webClient.method(method)
                .uri(uriBuilder -> {
                    URI uri = URI.create(url);
                    UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
                    if (queryParams != null) {
                        queryParams.forEach(builder::queryParam);
                    }
                    return builder.build().toUri();
                })
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        if (body != null && method != HttpMethod.GET) {
            request.bodyValue(body);
        }
        return request.retrieve()
                .onStatus(
                        status -> !status.is2xxSuccessful(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .map(error -> new RuntimeException("API failed: " + error))
                )
                .bodyToMono(responseType)
                .block();
    }
}
