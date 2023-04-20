package com.example.tnt.exception;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
@Builder
public class ErrorDetails {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date time;

    private String message;

    /**
     * Exception Type
     */
    public enum ExceptionTypeEnum {
        EXCEPTION("Exception"),

        API_EXCEPTION("ApiException");


        private final String value;

        ExceptionTypeEnum(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static ExceptionTypeEnum fromValue(String value) {
            for (ExceptionTypeEnum b : ExceptionTypeEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    private ExceptionTypeEnum exceptionType;

}