package com.example.tnt.model;

import lombok.*;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ApiResponse implements Serializable {
    private Map<String, Object> apiResponseBody;
    private UUID traceId;
    private UUID parentTraceId;
}
