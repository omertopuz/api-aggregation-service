package com.example.tnt.util;

import com.example.tnt.model.ApiRequest;
import com.example.tnt.model.ConsolidatedApiRequest;
import com.example.tnt.repository.RequestQueue;
import com.example.tnt.repository.ResponseMap;
import com.example.tnt.repository.inmemory.InMemoryRequestQueue;
import com.example.tnt.repository.inmemory.InMemoryResponseMap;
import com.example.tnt.service.BackendServicesClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@Disabled("disabled to diminish build time")
class ApiRequestHandlerTest {

    private ApiRequestHandler apiRequestHandler;
    private final BackendServicesClient backendServicesClient = Mockito.mock(BackendServicesClient.class);
    private final Integer bulkSize = 5;
    private final RequestQueue requestQueue = new InMemoryRequestQueue();
    private final ResponseMap responseMap = new InMemoryResponseMap();

    private final static int SLA_PER_ENDPOINT = 5;
    private final static int SLA_APPLICATION = 10;

    @BeforeEach
    void setUp(){
        Long maxWaitDurationBySeconds = 5L;
        apiRequestHandler = new ApiRequestHandler(backendServicesClient, bulkSize, maxWaitDurationBySeconds, requestQueue,responseMap);
        when(backendServicesClient.fetchBackendServicesResponse(any(),any()))
                .thenReturn(Mono.just(Map.of("key","value")));
    }

    @Test
    void shouldResponseImmediately_whenSubmitted5RequestForTheSameEndpoint_verifyingExternalApiCalledExactlyOnce(){
        LocalDateTime current = LocalDateTime.now();
        ConsolidatedApiRequest consolidatedApiRequest = new ConsolidatedApiRequest(Map.of("pricing","NL,CN"));
        for (int i = 0; i < bulkSize; i++) {
            apiRequestHandler.addRequest(consolidatedApiRequest);
        }
        apiRequestHandler.getConsolidatedApiResponse(consolidatedApiRequest);

        assertTrue(calculateDuration(current) <= 1);
        verify(backendServicesClient, times(1)).fetchBackendServicesResponse(any(),any());
    }
    @Test
    void shouldResponseIn5Seconds_whenSubmitted5RequestForTheDifferentEndpoints_verifyingExternalApiCalledMoreThanOnce(){
        LocalDateTime current = LocalDateTime.now();
        List<ConsolidatedApiRequest> incomingRequests = List.of(
                new ConsolidatedApiRequest(Map.of("pricing","p1,p1", "track", "t1,t1", "shipment","s1,s1"))
                , new ConsolidatedApiRequest(Map.of( "track", "t2,t2"))
                , new ConsolidatedApiRequest(Map.of("track","t3,t3", "shipment","s3,s3"))
                , new ConsolidatedApiRequest(Map.of("pricing","p4,p4", "shipment","s4,s4"))
                , new ConsolidatedApiRequest(Map.of("shipment","s5,s5"))
        );
        incomingRequests.forEach(incomingRequest-> apiRequestHandler.addRequest(incomingRequest));
        incomingRequests.forEach(incomingRequest-> apiRequestHandler.getConsolidatedApiResponse(incomingRequest));

        Map<String, Integer> endPointStatistics = new HashMap<>();
        incomingRequests.stream()
                .map(ConsolidatedApiRequest::getApiRequestMap)
                .forEach(apiRequestMap->
                    apiRequestMap.forEach((endpoint, apiRequest)->{
                        if (!endPointStatistics.containsKey(endpoint))
                            endPointStatistics.put(endpoint,0);
                        endPointStatistics.put(endpoint, endPointStatistics.get(endpoint) + 1);
                    })
                );

        assertTrue(calculateDuration(current) >= SLA_PER_ENDPOINT && calculateDuration(current) <= SLA_APPLICATION + 1);
        verify(backendServicesClient, times(endPointStatistics.size())).fetchBackendServicesResponse(any(),any());
    }
    @Test
    void shouldResponseIn5Seconds_whenSubmittedLessThan5Request_verifyingExternalApiCalledExactlyOnce(){
        LocalDateTime current = LocalDateTime.now();
        ConsolidatedApiRequest consolidatedApiRequest = new ConsolidatedApiRequest(Map.of("pricing","NL,CN"));
        for (int i = 0; i < bulkSize - 1; i++) {
            apiRequestHandler.addRequest(consolidatedApiRequest);
        }
        apiRequestHandler.getConsolidatedApiResponse(consolidatedApiRequest);

        assertTrue(calculateDuration(current) >= SLA_PER_ENDPOINT);
        verify(backendServicesClient, times(1)).fetchBackendServicesResponse(any(),any());
    }
    @Test
    void shouldResponseImmediately_whenThereIsRequestOlderThan5Seconds_verifyingExternalApiCalledExactlyOnce(){
        LocalDateTime current = LocalDateTime.now();
        UUID traceId = UUID.randomUUID();
        UUID parentTraceId = UUID.randomUUID();
        LocalDateTime requestTime = LocalDateTime.now().minusSeconds(5);
        ApiRequest apiRequest = new ApiRequest(requestTime, "NL,CN", parentTraceId);
        apiRequest.setRequestTime(requestTime);
        apiRequest.setTraceId(traceId);
        ConsolidatedApiRequest consolidatedApiRequest = new ConsolidatedApiRequest(Map.of("pricing","NL,CN"));
        consolidatedApiRequest.setRequestTime(requestTime);
        consolidatedApiRequest.setTraceId(parentTraceId);
        consolidatedApiRequest.setApiRequestMap(Map.of("pricing", apiRequest));

        apiRequestHandler.addRequest(consolidatedApiRequest);
        apiRequestHandler.getConsolidatedApiResponse(consolidatedApiRequest);

        assertTrue(calculateDuration(current) <= 1);
        verify(backendServicesClient, times(1)).fetchBackendServicesResponse(any(),any());
    }

    private long calculateDuration(LocalDateTime localDateTime){
        return Duration.between(localDateTime,LocalDateTime.now()).toSeconds();
    }
}