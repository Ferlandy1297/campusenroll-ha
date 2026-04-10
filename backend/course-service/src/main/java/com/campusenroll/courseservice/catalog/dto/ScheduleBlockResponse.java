package com.campusenroll.courseservice.catalog.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record ScheduleBlockResponse(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime) {
}
