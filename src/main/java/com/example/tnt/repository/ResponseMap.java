package com.example.tnt.repository;

import com.example.tnt.model.ApiResponse;

import java.util.UUID;

public interface ResponseMap {
    void addResponse(ApiResponse apiResponse);
    ApiResponse consumeResponse(UUID traceId);

}
