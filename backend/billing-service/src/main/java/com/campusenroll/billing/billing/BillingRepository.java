package com.campusenroll.billing.billing;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingRepository extends JpaRepository<Billing, Long> {

    List<Billing> findAllByOrderByCreatedAtDescIdDesc();

    boolean existsByPendingEnrollmentKey(Long pendingEnrollmentKey);

    boolean existsByPendingEnrollmentKeyAndIdNot(Long pendingEnrollmentKey, Long id);
}
