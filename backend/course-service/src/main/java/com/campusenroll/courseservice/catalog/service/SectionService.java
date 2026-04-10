package com.campusenroll.courseservice.catalog.service;

import com.campusenroll.courseservice.catalog.dto.ScheduleBlockRequest;
import com.campusenroll.courseservice.catalog.dto.ScheduleBlockResponse;
import com.campusenroll.courseservice.catalog.dto.SectionRequest;
import com.campusenroll.courseservice.catalog.dto.SectionResponse;
import com.campusenroll.courseservice.catalog.exception.BadRequestException;
import com.campusenroll.courseservice.catalog.model.ScheduleBlock;
import com.campusenroll.courseservice.catalog.model.Section;
import com.campusenroll.courseservice.catalog.repository.SectionRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;
    private final CourseService courseService;
    private final AcademicPeriodService academicPeriodService;

    public SectionService(
            SectionRepository sectionRepository,
            CourseService courseService,
            AcademicPeriodService academicPeriodService) {
        this.sectionRepository = sectionRepository;
        this.courseService = courseService;
        this.academicPeriodService = academicPeriodService;
    }

    @Transactional(readOnly = true)
    public List<SectionResponse> getSections() {
        return sectionRepository.findAll(Sort.by("sectionCode").ascending()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SectionResponse createSection(SectionRequest request) {
        validateScheduleBlocks(request.scheduleBlocks());

        Section section = new Section();
        section.setSectionCode(normalize(request.sectionCode()));
        section.setCapacity(request.capacity());
        section.setActive(request.active());
        section.setCourse(courseService.findCourse(request.courseId()));
        section.setAcademicPeriod(academicPeriodService.findPeriod(request.academicPeriodId()));
        section.setScheduleBlocks(request.scheduleBlocks().stream()
                .map(this::toScheduleBlock)
                .toList());

        return toResponse(sectionRepository.save(section));
    }

    private SectionResponse toResponse(Section section) {
        return new SectionResponse(
                section.getId(),
                section.getSectionCode(),
                section.getCapacity(),
                section.getActive(),
                section.getCourse().getId(),
                section.getCourse().getCourseCode(),
                section.getAcademicPeriod().getId(),
                section.getAcademicPeriod().getName(),
                section.getScheduleBlocks().stream()
                        .map(this::toResponse)
                        .toList());
    }

    private void validateScheduleBlocks(List<ScheduleBlockRequest> scheduleBlocks) {
        boolean hasInvalidRange = scheduleBlocks.stream()
                .anyMatch(block -> !block.endTime().isAfter(block.startTime()));

        if (hasInvalidRange) {
            throw new BadRequestException("endTime must be after startTime");
        }
    }

    private ScheduleBlock toScheduleBlock(ScheduleBlockRequest request) {
        ScheduleBlock scheduleBlock = new ScheduleBlock();
        scheduleBlock.setDayOfWeek(request.dayOfWeek());
        scheduleBlock.setStartTime(request.startTime());
        scheduleBlock.setEndTime(request.endTime());
        return scheduleBlock;
    }

    private ScheduleBlockResponse toResponse(ScheduleBlock scheduleBlock) {
        return new ScheduleBlockResponse(
                scheduleBlock.getDayOfWeek(),
                scheduleBlock.getStartTime(),
                scheduleBlock.getEndTime());
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
