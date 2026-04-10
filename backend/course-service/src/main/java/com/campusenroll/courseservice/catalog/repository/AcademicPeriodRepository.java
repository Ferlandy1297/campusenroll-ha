package com.campusenroll.courseservice.catalog.repository;

import com.campusenroll.courseservice.catalog.model.AcademicPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicPeriodRepository extends JpaRepository<AcademicPeriod, Long> {
}
