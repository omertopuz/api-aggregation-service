package com.example.tnt.repository.inmemory;

import com.example.tnt.model.ApiRequest;
import com.example.tnt.repository.RequestQueue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

public class InMemoryRequestQueue implements RequestQueue {
    private final Map<String, ConcurrentLinkedQueue<ApiRequest>> requestQueueMap;
    private final ReentrantLock reentrantLock = new ReentrantLock();
    public InMemoryRequestQueue() {
        this.requestQueueMap = new HashMap<>();
    }

    @Override
    public void pushRequest(String queueName, ApiRequest apiRequest) {
        if (apiRequest != null){
            reentrantLock.lock();
            try {
                if (!requestQueueMap.containsKey(queueName))
                    requestQueueMap.put(queueName, new ConcurrentLinkedQueue<>());
                requestQueueMap.get(queueName).add(apiRequest);
            }finally {
                reentrantLock.unlock();
            }
        }
    }

    @Override
    public ApiRequest pollRequest(String queueName) {
        return requestQueueMap.get(queueName).poll();
    }

    @Override
    public ApiRequest peekRequest(String queueName) {
        return requestQueueMap.get(queueName).peek();
    }

    @Override
    public Set<String> getQueueNames() {
        return requestQueueMap.keySet();
    }

    @Override
    public int size(String queueName) {
        return requestQueueMap.get(queueName).size();
    }
}
