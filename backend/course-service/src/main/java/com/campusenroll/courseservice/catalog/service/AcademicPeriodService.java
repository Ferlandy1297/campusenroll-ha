package com.campusenroll.courseservice.catalog.service;

import com.campusenroll.courseservice.catalog.cache.CatalogCacheNames;
import com.campusenroll.courseservice.catalog.dto.AcademicPeriodRequest;
import com.campusenroll.courseservice.catalog.dto.AcademicPeriodResponse;
import com.campusenroll.courseservice.catalog.exception.ResourceNotFoundException;
import com.campusenroll.courseservice.catalog.model.AcademicPeriod;
import com.campusenroll.courseservice.catalog.repository.AcademicPeriodRepository;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcademicPeriodService {

    private final AcademicPeriodRepository academicPeriodRepository;

    public AcademicPeriodService(AcademicPeriodRepository academicPeriodRepository) {
        this.academicPeriodRepository = academicPeriodRepository;
    }

    @Cacheable(cacheNames = CatalogCacheNames.ACADEMIC_PERIODS)
    @Transactional(readOnly = true)
    public List<AcademicPeriodResponse> getPeriods() {
        return academicPeriodRepository.findAll(Sort.by("name").ascending()).stream()
                .map(this::toResponse)
                .toList();
    }

    @CacheEvict(cacheNames = CatalogCacheNames.ACADEMIC_PERIODS, allEntries = true)
    @Transactional
    public AcademicPeriodResponse createPeriod(AcademicPeriodRequest request) {
        AcademicPeriod academicPeriod = new AcademicPeriod();
        academicPeriod.setName(normalize(request.name()));
        academicPeriod.setActive(request.active());

        return toResponse(academicPeriodRepository.save(academicPeriod));
    }

    @Transactional(readOnly = true)
    public AcademicPeriod findPeriod(Long id) {
        return academicPeriodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic period not found for id " + id));
    }

    private AcademicPeriodResponse toResponse(AcademicPeriod academicPeriod) {
        return new AcademicPeriodResponse(
                academicPeriod.getId(),
                academicPeriod.getName(),
                academicPeriod.getActive());
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
