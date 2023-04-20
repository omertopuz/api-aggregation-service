package com.example.tnt.model;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsolidatedApiResponse {
    private  Map<String, Object> pricing;
    private Map<String, Object> track;
    private Map<String, Object> shipments;
}
