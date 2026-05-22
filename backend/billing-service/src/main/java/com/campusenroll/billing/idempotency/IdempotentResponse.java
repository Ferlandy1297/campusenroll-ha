package com.campusenroll.billing.idempotency;

public record IdempotentResponse<T>(int status, T body) {

    public static <T> IdempotentResponse<T> created(T body) {
        return new IdempotentResponse<>(201, body);
    }
}
