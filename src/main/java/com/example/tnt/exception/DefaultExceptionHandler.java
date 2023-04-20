package com.example.tnt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;

@RestControllerAdvice
public class DefaultExceptionHandler {
    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDetails unKnownException(Exception ex) {
        return ErrorDetails.builder()
                .exceptionType(ErrorDetails.ExceptionTypeEnum.EXCEPTION)
                .message("Unexpected Error occurred: "+ex.getMessage())
                .time(new Date())
                .build();
    }
    @ExceptionHandler(value = {IllegalStateException.class})
    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorDetails redisConnection(IllegalStateException ex) {
        String rootCause = ex.getMessage() != null
                && ex.getMessage().contains("LettuceConnectionFactory ") ? "Redis server is unreachable by the application. Check network settings between the application and redis": "Unexpected Error occurred:";
        return ErrorDetails.builder()
                .exceptionType(ErrorDetails.ExceptionTypeEnum.EXCEPTION)
                .message(rootCause+ ": "+ex.getMessage())
                .time(new Date())
                .build();
    }

}
