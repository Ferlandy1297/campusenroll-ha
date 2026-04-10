package com.campusenroll.courseservice.catalog.controller;

import com.campusenroll.courseservice.catalog.dto.AcademicPeriodRequest;
import com.campusenroll.courseservice.catalog.dto.AcademicPeriodResponse;
import com.campusenroll.courseservice.catalog.service.AcademicPeriodService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/periods")
public class AcademicPeriodController {

    private final AcademicPeriodService academicPeriodService;

    public AcademicPeriodController(AcademicPeriodService academicPeriodService) {
        this.academicPeriodService = academicPeriodService;
    }

    @GetMapping
    public List<AcademicPeriodResponse> getPeriods() {
        return academicPeriodService.getPeriods();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AcademicPeriodResponse createPeriod(@Valid @RequestBody AcademicPeriodRequest request) {
        return academicPeriodService.createPeriod(request);
    }
}
