package com.example.tnt.repository;

import com.example.tnt.model.ApiRequest;

import java.util.Set;

public interface RequestQueue {
    void pushRequest(String queueName, ApiRequest apiRequest);
    ApiRequest pollRequest(String queueName);
    ApiRequest peekRequest(String queueName);
    int size(String queueName);
    Set<String> getQueueNames();
}
