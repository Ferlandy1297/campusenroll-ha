package com.campusenroll.courseservice.catalog.dto;

import java.util.List;

public record SectionResponse(
        Long id,
        String sectionCode,
        Integer capacity,
        Boolean active,
        Long courseId,
        String courseCode,
        Long academicPeriodId,
        String academicPeriodName,
        List<ScheduleBlockResponse> scheduleBlocks) {
}
