package com.example.tnt.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientRequest;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
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
        String uri = endpoint.concat("?q=".concat(params));
        return webClient.get().uri(uri)
                .httpRequest(httpRequest -> {
                    HttpClientRequest reactorRequest = httpRequest.getNativeRequest();
                    reactorRequest.responseTimeout(Duration.ofSeconds(timeOutDurationByMillis));
                })
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofMillis(timeOutDurationByMillis))
                .onErrorResume(Throwable.class, throwable ->  mapErrors(throwable, uri))
        ;
    }

    /**
     * assumption for the external api just response with 200 or 503 so the other types are ignored
     * @param throwable exception
     * @param uri request path with params
     * @return empty map
     */
    private Mono<? extends Map<String, String>> mapErrors(Throwable throwable, String uri) {
        log.error("Failing request for the uri: {}", uri);
        if (throwable instanceof WebClientRequestException)
            log.error("Connection refused by the external api", throwable);
        if (throwable instanceof TimeoutException)
            log.error("Request get a time out for the uri: {}", uri);
        else if (throwable instanceof WebClientResponseException exception) {
            if (exception.getStatusCode().is5xxServerError())
                log.error("External Api is unavailable",throwable);
        }
        else
            log.error("An error occurred for the request.", throwable);

        return Mono.just(Map.of());
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
