package com.example.tnt.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class ConsolidatedApiRequest {
    private UUID traceId;
    private LocalDateTime requestTime;
    private Map<String, ApiRequest> apiRequestMap;

    public ConsolidatedApiRequest(Map<String, String> endpointParamsMap) {
        this.apiRequestMap = new HashMap<>();
        this.traceId = UUID.randomUUID();
        this.requestTime = LocalDateTime.now();
        endpointParamsMap
                .entrySet()
                .stream()
                .filter(entry-> entry.getValue() != null)
                .forEach(entry->this.apiRequestMap.put(entry.getKey(), new ApiRequest(requestTime, entry.getValue(),this.traceId)));

    }
}
