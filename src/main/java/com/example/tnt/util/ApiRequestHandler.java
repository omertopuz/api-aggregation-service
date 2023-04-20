package com.example.tnt.util;

import com.example.tnt.model.ApiRequest;
import com.example.tnt.model.ApiResponse;
import com.example.tnt.model.ConsolidatedApiRequest;
import com.example.tnt.repository.RequestQueue;
import com.example.tnt.repository.ResponseMap;
import com.example.tnt.service.BackendServicesClient;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class ApiRequestHandler {
    private final BackendServicesClient backendServicesClient;
    private final Integer bulkSize;
    private final Long maxWaitDurationBySeconds;
    private final static int SCHEDULER_PERIOD = 10;
    private final RequestQueue requestQueue;
    private final ResponseMap responseMap;

    public ApiRequestHandler(BackendServicesClient backendServicesClient
            , Integer bulkSize
            , Long maxWaitDurationBySeconds
            , RequestQueue requestQueue
            , ResponseMap responseMap) {
        this.backendServicesClient  = backendServicesClient;
        this.bulkSize = bulkSize;
        this.maxWaitDurationBySeconds = maxWaitDurationBySeconds;
        this.responseMap = responseMap;
        this.requestQueue = requestQueue;

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(this::processCombinedRequest, 0, SCHEDULER_PERIOD, TimeUnit.MILLISECONDS);
    }

    public void addRequest(ConsolidatedApiRequest consolidatedApiRequest){
        consolidatedApiRequest.getApiRequestMap()
                        .forEach(requestQueue::pushRequest);
    }

    private boolean checkCallApiAvailable(String queueName){
        return checkBulkSizeExceeded(queueName) || checkOldestItem(queueName);
    }

    private boolean checkOldestItem(String queueName){
        return requestQueue.peekRequest(queueName) != null &&
                Math.abs(Duration.between(requestQueue.peekRequest(queueName).getRequestTime(), LocalDateTime.now())
                        .toSeconds()) >= maxWaitDurationBySeconds;
    }
    private boolean checkBulkSizeExceeded(String queueName){
        return requestQueue.size(queueName) >= bulkSize;
    }

    private List<ApiRequest> extractRequestList(String queueName){
        if(checkCallApiAvailable(queueName)){
            List<ApiRequest> apiRequestList = new LinkedList<>();
            while (checkBulkSizeExceeded(queueName) || checkOldestItem(queueName))
                apiRequestList.add(requestQueue.pollRequest(queueName));
            return apiRequestList;
        }
        return null;
    }

    private void processCombinedRequest() {
        requestQueue.getQueueNames().forEach(requestPath->{
            List<ApiRequest> apiRequestList = extractRequestList(requestPath);
            if (apiRequestList != null && apiRequestList.size()> 0){
                fetchResponses(requestPath, apiRequestList);
            }
        });
    }

    private void fetchResponses(String uriPath, List<ApiRequest> apiRequestList){
        backendServicesClient
                .fetchBackendServicesResponse(uriPath, buildParams(apiRequestList))
                .subscribe(responses-> apiRequestList.forEach(apiRequest-> putResponseToResponseMap(responses, apiRequest)));
    }

    private void putResponseToResponseMap(Map responses, ApiRequest apiRequest){
        ApiResponse apiResponse = ApiResponse.builder()
                .traceId(apiRequest.getTraceId())
                .parentTraceId(apiRequest.getParentTraceId())
                .apiResponseBody(buildresponseMap(apiRequest.buildParamsSet(), responses))
                .build();
        log.debug("Response received: {}", apiResponse);
        responseMap.addResponse(apiResponse);
    }

    private Map<String,Object> buildresponseMap(Set<String> paramsSet, Map<String, Object> monoMap){
        Map<String, Object> responseMap = new HashMap<>();
        for (String param : paramsSet) {
            responseMap.put(param, monoMap.get(param));
        }
        return responseMap;
    }

    private String buildParams(List<ApiRequest> apiRequestList){
        return apiRequestList.stream().map(ApiRequest::getParams).collect(Collectors.joining(","));
    }

    public Map<String, Map<String,Object>> getConsolidatedApiResponse(ConsolidatedApiRequest consolidatedApiRequest) {
        Map<UUID, String> traceIdPathMap = consolidatedApiRequest.getApiRequestMap()
                .entrySet().stream().collect(Collectors.toMap(e -> e.getValue().getTraceId(), Map.Entry::getKey));

        return consolidatedApiRequest.getApiRequestMap().values()
                .stream()
                .filter(Objects::nonNull)
                .map(this::getApiResponseAsync)
                .map(CompletableFuture::join)
                .collect(Collectors.toMap(resp-> traceIdPathMap.get(resp.getTraceId()), ApiResponse::getApiResponseBody));
    }

    private CompletableFuture<ApiResponse> getApiResponseAsync(ApiRequest apiRequest) {
        return CompletableFuture.supplyAsync(()-> responseMap.consumeResponse(apiRequest.getTraceId()));
    }
}
