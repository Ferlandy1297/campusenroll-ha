package com.campusenroll.courseservice.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.campusenroll.courseservice.catalog.dto.CourseResponse;
import com.campusenroll.courseservice.catalog.dto.ScheduleBlockResponse;
import com.campusenroll.courseservice.catalog.dto.SectionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializer;

class CacheConfigurationTest {

    private final CacheConfiguration cacheConfiguration = new CacheConfiguration();

    @Test
    void shouldRoundTripCourseResponsesWithConcreteTypes() {
        RedisSerializer<Object> serializer = cacheConfiguration.redisListValueSerializer(CourseResponse.class);
        List<CourseResponse> expected = List.of(new CourseResponse(1L, "CS101", "Intro to Programming", 4, true));
        ObjectMapper responseObjectMapper = responseObjectMapper();

        Object actual = serializer.deserialize(serializer.serialize(expected));

        assertNotNull(actual);
        List<?> cachedCourses = assertInstanceOf(List.class, actual);
        assertEquals(1, cachedCourses.size());
        assertInstanceOf(CourseResponse.class, cachedCourses.get(0));
        assertEquals(expected, actual);
        assertDoesNotThrow(() -> responseObjectMapper
                .writerFor(responseObjectMapper.getTypeFactory().constructCollectionType(List.class, CourseResponse.class))
                .writeValueAsString(actual));
    }

    @Test
    void shouldRoundTripSectionResponsesWithJavaTimeFields() {
        RedisSerializer<Object> serializer = cacheConfiguration.redisListValueSerializer(SectionResponse.class);
        List<SectionResponse> expected = List.of(new SectionResponse(
                1L,
                "CS101-A",
                30,
                true,
                1L,
                "CS101",
                2L,
                "2026-A",
                List.of(new ScheduleBlockResponse(
                        DayOfWeek.MONDAY,
                        LocalTime.of(8, 0),
                        LocalTime.of(9, 30)))));
        ObjectMapper responseObjectMapper = responseObjectMapper();

        Object actual = serializer.deserialize(serializer.serialize(expected));

        assertNotNull(actual);
        List<?> cachedSections = assertInstanceOf(List.class, actual);
        assertEquals(1, cachedSections.size());
        assertInstanceOf(SectionResponse.class, cachedSections.get(0));
        assertEquals(expected, actual);
        assertDoesNotThrow(() -> responseObjectMapper
                .writerFor(responseObjectMapper.getTypeFactory().constructCollectionType(List.class, SectionResponse.class))
                .writeValueAsString(actual));
    }

    private ObjectMapper responseObjectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }
}
