package com.campusenroll.courseservice.catalog.repository;

import com.campusenroll.courseservice.catalog.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectionRepository extends JpaRepository<Section, Long> {
}
