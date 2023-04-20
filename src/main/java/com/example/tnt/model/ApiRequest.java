package com.example.tnt.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class ApiRequest implements Serializable {
    @Indexed
    @Id
    private UUID traceId;
    private UUID parentTraceId;
    private LocalDateTime requestTime;
    private String params;

    public ApiRequest() {
    }

    public ApiRequest(LocalDateTime requestTime, String params, UUID parentTraceId) {
        this.traceId = UUID.randomUUID();
        this.requestTime = requestTime;
        this.params = params;
        this.parentTraceId = parentTraceId;
    }

    public Set<String> buildParamsSet(){
        return Arrays.stream(params.split(",")).collect(Collectors.toSet());
    }
}
