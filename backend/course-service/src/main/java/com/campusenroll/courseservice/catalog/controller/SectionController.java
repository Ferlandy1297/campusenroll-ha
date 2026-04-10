package com.campusenroll.courseservice.catalog.controller;

import com.campusenroll.courseservice.catalog.dto.SectionRequest;
import com.campusenroll.courseservice.catalog.dto.SectionResponse;
import com.campusenroll.courseservice.catalog.service.SectionService;
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
@RequestMapping("/api/sections")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping
    public List<SectionResponse> getSections() {
        return sectionService.getSections();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SectionResponse createSection(@Valid @RequestBody SectionRequest request) {
        return sectionService.createSection(request);
    }
}
