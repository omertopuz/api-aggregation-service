package com.example.tnt.service;

import com.example.tnt.model.ConsolidatedApiRequest;
import com.example.tnt.repository.RequestQueue;
import com.example.tnt.repository.ResponseMap;
import com.example.tnt.util.ApiRequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class ConsolidatedApiService {
    private final ApiRequestHandler apiRequestHandler;

    public ConsolidatedApiService(BackendServicesClient backendServicesClient
            , @Value("${aggregation.bulk-size:5}") Integer bulkSize
            , @Value("${aggregation.max-wait-duration-by-seconds:5}") Long maxWaitDurationBySeconds
            , RequestQueue requestQueue
            , ResponseMap responseMap
    ) {
        apiRequestHandler = new ApiRequestHandler(backendServicesClient, bulkSize, maxWaitDurationBySeconds, requestQueue, responseMap);
    }

    public Map<String, Map<String,Object>> getConsolidatedApiResponse(ConsolidatedApiRequest consolidatedApiRequest) {
        apiRequestHandler.addRequest(consolidatedApiRequest);
        return apiRequestHandler.getConsolidatedApiResponse(consolidatedApiRequest);
    }
}
