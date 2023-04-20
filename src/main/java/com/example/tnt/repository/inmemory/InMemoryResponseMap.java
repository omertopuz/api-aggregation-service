package com.example.tnt.repository.inmemory;

import com.example.tnt.model.ApiResponse;
import com.example.tnt.repository.ResponseMap;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

public class InMemoryResponseMap implements ResponseMap {

    private final ConcurrentMap<UUID, ApiResponse> responseMap;
    private final ReentrantLock reentrantLock = new ReentrantLock();


    public InMemoryResponseMap() {
        this.responseMap = new ConcurrentHashMap<>();
    }

    @Override
    public void addResponse(ApiResponse apiResponse) {
        reentrantLock.lock();
        try {
            responseMap.put(apiResponse.getTraceId(), apiResponse);
        }finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public ApiResponse consumeResponse(UUID traceId) {
        while (!responseMap.containsKey(traceId));
        return responseMap.remove(traceId);
    }
}
