package com.campusenroll.billing.idempotency;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByServiceNameAndOperationNameAndIdempotencyKey(
            String serviceName,
            String operationName,
            String idempotencyKey);
}
