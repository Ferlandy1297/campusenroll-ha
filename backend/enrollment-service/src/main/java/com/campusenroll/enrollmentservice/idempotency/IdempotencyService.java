package com.campusenroll.enrollmentservice.idempotency;

import com.campusenroll.enrollmentservice.error.ConflictException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {

    private static final String PAYLOAD_MISMATCH_MESSAGE =
            "Idempotency key was reused with a different payload";
    private static final String REQUEST_IN_PROGRESS_MESSAGE =
            "A request with this idempotency key is already in progress";

    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    public IdempotencyService(
            IdempotencyRecordRepository idempotencyRecordRepository,
            ObjectMapper objectMapper,
            EntityManager entityManager) {
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
    }

    public <T> IdempotentResponse<T> execute(
            String serviceName,
            String operationName,
            String idempotencyKey,
            Object requestBody,
            Class<T> responseType,
            Supplier<IdempotentResponse<T>> action) {
        String normalizedKey = idempotencyKey.trim();
        String requestHash = hashRequest(requestBody);

        ReservationOutcome<T> reservation = reserve(
                serviceName,
                operationName,
                normalizedKey,
                requestHash,
                responseType);

        if (reservation.replayedResponse() != null) {
            return reservation.replayedResponse();
        }

        IdempotentResponse<T> response = action.get();
        complete(reservation.record(), response);
        return response;
    }

    private <T> ReservationOutcome<T> reserve(
            String serviceName,
            String operationName,
            String idempotencyKey,
            String requestHash,
            Class<T> responseType) {
        IdempotencyRecord record = new IdempotencyRecord();
        record.setServiceName(serviceName);
        record.setOperationName(operationName);
        record.setIdempotencyKey(idempotencyKey);
        record.setRequestHash(requestHash);
        record.setStatus(IdempotencyRecordStatus.IN_PROGRESS);
        record.setCreatedAt(OffsetDateTime.now());

        int insertedRows = entityManager.createNativeQuery("""
                        INSERT INTO idempotency_records (
                            service_name,
                            operation_name,
                            idempotency_key,
                            request_hash,
                            response_status,
                            response_body,
                            status,
                            created_at,
                            completed_at
                        )
                        VALUES (
                            :serviceName,
                            :operationName,
                            :idempotencyKey,
                            :requestHash,
                            NULL,
                            NULL,
                            :status,
                            :createdAt,
                            NULL
                        )
                        ON CONFLICT (service_name, operation_name, idempotency_key) DO NOTHING
                        """)
                .setParameter("serviceName", record.getServiceName())
                .setParameter("operationName", record.getOperationName())
                .setParameter("idempotencyKey", record.getIdempotencyKey())
                .setParameter("requestHash", record.getRequestHash())
                .setParameter("status", record.getStatus().name())
                .setParameter("createdAt", record.getCreatedAt())
                .executeUpdate();

        IdempotencyRecord existingRecord = idempotencyRecordRepository
                .findByServiceNameAndOperationNameAndIdempotencyKey(serviceName, operationName, idempotencyKey)
                .orElseThrow(() -> new IllegalStateException("Reserved idempotency record was not found"));

        if (insertedRows == 1) {
            return ReservationOutcome.owned(existingRecord);
        }

        if (!existingRecord.getRequestHash().equals(requestHash)) {
            throw new ConflictException(PAYLOAD_MISMATCH_MESSAGE);
        }

        if (existingRecord.getStatus() == IdempotencyRecordStatus.COMPLETED) {
            return ReservationOutcome.replayed(replay(existingRecord, responseType));
        }

        throw new ConflictException(REQUEST_IN_PROGRESS_MESSAGE);
    }

    private void complete(IdempotencyRecord record, IdempotentResponse<?> response) {
        record.setResponseStatus(response.status());
        record.setResponseBody(serialize(response.body()));
        record.setStatus(IdempotencyRecordStatus.COMPLETED);
        record.setCompletedAt(OffsetDateTime.now());
        idempotencyRecordRepository.saveAndFlush(record);
    }

    private <T> IdempotentResponse<T> replay(IdempotencyRecord record, Class<T> responseType) {
        if (record.getResponseStatus() == null || record.getResponseBody() == null) {
            throw new IllegalStateException("Stored idempotency response is incomplete");
        }

        try {
            T responseBody = objectMapper.readValue(record.getResponseBody(), responseType);
            return new IdempotentResponse<>(record.getResponseStatus(), responseBody);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize stored idempotency response", ex);
        }
    }

    private String hashRequest(Object requestBody) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] serializedRequest = objectMapper.writeValueAsBytes(requestBody);
            return HexFormat.of().formatHex(digest.digest(serializedRequest));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize request for idempotency hashing", ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private String serialize(Object responseBody) {
        try {
            return objectMapper.writeValueAsString(responseBody);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize idempotency response", ex);
        }
    }

    private record ReservationOutcome<T>(IdempotencyRecord record, IdempotentResponse<T> replayedResponse) {

        private static <T> ReservationOutcome<T> owned(IdempotencyRecord record) {
            return new ReservationOutcome<>(record, null);
        }

        private static <T> ReservationOutcome<T> replayed(IdempotentResponse<T> response) {
            return new ReservationOutcome<>(null, response);
        }
    }
}
