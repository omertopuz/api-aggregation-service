package com.example.tnt.controller;

import com.example.tnt.model.ConsolidatedApiRequest;
import com.example.tnt.model.ConsolidatedApiResponse;
import com.example.tnt.service.ConsolidatedApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/aggregation")
public class ConsolidatedApiController {
    private final ConsolidatedApiService consolidatedApiService;
    private final String pathPricing;
    private final String pathTrack;
    private final String pathShipments;

    public ConsolidatedApiController(ConsolidatedApiService consolidatedApiService
            , @Value("${backend-services.path.pricing:pricing}") String pathPricing
            , @Value("${backend-services.path.track:track}") String pathTrack
            , @Value("${backend-services.path.shipments:shipments}") String pathShipments) {
        this.consolidatedApiService = consolidatedApiService;
        this.pathPricing = pathPricing;
        this.pathTrack = pathTrack;
        this.pathShipments = pathShipments;
    }

    @GetMapping
    public ConsolidatedApiResponse getAggregatedApiResponse(@RequestParam(required = false) String pricing, @RequestParam(required = false) String track, @RequestParam(required = false) String shipments){
        Map<String, String> pathParamMap = new HashMap<>() {{
            put(pathPricing, pricing);
            put(pathTrack, track);
            put(pathShipments, shipments);
        }};
        ConsolidatedApiRequest consolidatedApiRequest = new ConsolidatedApiRequest(pathParamMap);
        Map<String, Map<String,Object>> response = consolidatedApiService.getConsolidatedApiResponse(consolidatedApiRequest);
        logRequest(consolidatedApiRequest);
        return ConsolidatedApiResponse.builder()
                .pricing(response.get(pathPricing))
                .track(response.get(pathTrack))
                .shipments(response.get(pathShipments))
                .build();
    }

    private void logRequest(ConsolidatedApiRequest consolidatedApiRequest){
        long totalRequestDuration = Math.abs(Duration.between(LocalDateTime.now(), consolidatedApiRequest.getRequestTime()).toMillis());
        log.debug("request completed with |totalRequestDuration: {}, traceId: {}|",totalRequestDuration, consolidatedApiRequest.getTraceId());
        if (Duration.between(LocalDateTime.now(), consolidatedApiRequest.getRequestTime()).toMillis() > 11000)
            log.error("request completed with |totalRequestDuration: {}, traceId: {}|",totalRequestDuration, consolidatedApiRequest.getTraceId());

    }
}
