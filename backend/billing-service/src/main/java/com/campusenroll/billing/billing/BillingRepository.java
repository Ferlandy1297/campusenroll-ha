package com.campusenroll.billing.billing;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingRepository extends JpaRepository<Billing, Long> {

    List<Billing> findAllByOrderByCreatedAtDescIdDesc();

    boolean existsByEnrollmentIdAndStatus(Long enrollmentId, BillingStatus status);

    boolean existsByEnrollmentIdAndStatusAndIdNot(Long enrollmentId, BillingStatus status, Long id);
}
