package com.example.tnt.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Map;

@Component
public class BackendServicesClient {

    private final WebClient webClient;
    private final long timeOutDurationByMillis;

    public BackendServicesClient(
            @Value("${backend-services.base-url:http://localhost:8880/}") String baseUrl
            , @Value("${backend-services.timeoutByMillis:5000}") long timeOutDurationByMillis
    ) {
        this.timeOutDurationByMillis = timeOutDurationByMillis;
        this.webClient = buildWebClient(baseUrl, timeOutDurationByMillis);
    }

    public Mono<Map> fetchBackendServicesResponse(String endpoint, String params){
        return webClient.get().uri(endpoint.concat("?q=".concat(params)))
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofMillis(timeOutDurationByMillis))
                .onErrorReturn(Map.of())
                .onErrorResume(WebClientResponseException.class, this::serviceUnAvailableRule);
    }

    private Mono<? extends Map> serviceUnAvailableRule(WebClientResponseException ex) {
        return ex.getStatusCode().is5xxServerError() ? Mono.just(Map.of()) : Mono.error(ex);
    }

    private WebClient buildWebClient(String baseUrl, long timeOutDurationByMillis){
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.newConnection()
                        .responseTimeout(Duration.ofMillis(timeOutDurationByMillis))
                        .wiretap(true)))
                .build();
    }

}
