package com.campusenroll.courseservice.catalog.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusenroll.courseservice.catalog.cache.CatalogCacheNames;
import com.campusenroll.courseservice.catalog.dto.AcademicPeriodRequest;
import com.campusenroll.courseservice.catalog.dto.CourseRequest;
import com.campusenroll.courseservice.catalog.dto.ScheduleBlockRequest;
import com.campusenroll.courseservice.catalog.dto.SectionRequest;
import com.campusenroll.courseservice.catalog.model.AcademicPeriod;
import com.campusenroll.courseservice.catalog.model.Course;
import com.campusenroll.courseservice.catalog.model.Section;
import com.campusenroll.courseservice.catalog.repository.AcademicPeriodRepository;
import com.campusenroll.courseservice.catalog.repository.CourseRepository;
import com.campusenroll.courseservice.catalog.repository.SectionRepository;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

class CatalogCachingTest {

    @Test
    void shouldCacheAndEvictCourses() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(CourseCachingTestConfiguration.class)) {
            CourseRepository courseRepository = context.getBean(CourseRepository.class);
            CourseService courseService = context.getBean(CourseService.class);

            Course firstCourse = course(1L, "CS101", "Introduction to Programming");
            Course secondCourse = course(2L, "DB201", "Database Systems");

            when(courseRepository.findAll(any(Sort.class)))
                    .thenReturn(List.of(firstCourse), List.of(firstCourse, secondCourse));
            when(courseRepository.existsByCourseCodeIgnoreCase("DB201")).thenReturn(false);
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
                Course course = invocation.getArgument(0);
                course.setId(2L);
                return course;
            });

            courseService.getCourses();
            courseService.getCourses();
            verify(courseRepository, times(1)).findAll(any(Sort.class));

            courseService.createCourse(new CourseRequest("DB201", "Database Systems", 4, true));
            courseService.getCourses();
            verify(courseRepository, times(2)).findAll(any(Sort.class));
        }
    }

    @Test
    void shouldCacheAndEvictAcademicPeriods() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AcademicPeriodCachingTestConfiguration.class)) {
            AcademicPeriodRepository academicPeriodRepository = context.getBean(AcademicPeriodRepository.class);
            AcademicPeriodService academicPeriodService = context.getBean(AcademicPeriodService.class);

            AcademicPeriod firstPeriod = academicPeriod(1L, "2026-A");
            AcademicPeriod secondPeriod = academicPeriod(2L, "2026-B");

            when(academicPeriodRepository.findAll(any(Sort.class)))
                    .thenReturn(List.of(firstPeriod), List.of(firstPeriod, secondPeriod));
            when(academicPeriodRepository.save(any(AcademicPeriod.class))).thenAnswer(invocation -> {
                AcademicPeriod period = invocation.getArgument(0);
                period.setId(2L);
                return period;
            });

            academicPeriodService.getPeriods();
            academicPeriodService.getPeriods();
            verify(academicPeriodRepository, times(1)).findAll(any(Sort.class));

            academicPeriodService.createPeriod(new AcademicPeriodRequest("2026-B", true));
            academicPeriodService.getPeriods();
            verify(academicPeriodRepository, times(2)).findAll(any(Sort.class));
        }
    }

    @Test
    void shouldCacheAndEvictSections() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(SectionCachingTestConfiguration.class)) {
            SectionRepository sectionRepository = context.getBean(SectionRepository.class);
            CourseService courseService = context.getBean(CourseService.class);
            AcademicPeriodService academicPeriodService = context.getBean(AcademicPeriodService.class);
            SectionService sectionService = context.getBean(SectionService.class);

            Course course = course(1L, "CS101", "Introduction to Programming");
            AcademicPeriod academicPeriod = academicPeriod(1L, "2026-A");
            Section firstSection = section(1L, "CS101-A", course, academicPeriod);
            Section secondSection = section(2L, "CS101-B", course, academicPeriod);

            when(sectionRepository.findAll(any(Sort.class)))
                    .thenReturn(List.of(firstSection), List.of(firstSection, secondSection));
            when(courseService.findCourse(1L)).thenReturn(course);
            when(academicPeriodService.findPeriod(1L)).thenReturn(academicPeriod);
            when(sectionRepository.save(any(Section.class))).thenAnswer(invocation -> {
                Section section = invocation.getArgument(0);
                section.setId(2L);
                return section;
            });

            sectionService.getSections();
            sectionService.getSections();
            verify(sectionRepository, times(1)).findAll(any(Sort.class));

            sectionService.createSection(new SectionRequest(
                    "CS101-B",
                    25,
                    true,
                    1L,
                    1L,
                    List.of(new ScheduleBlockRequest(
                            DayOfWeek.MONDAY,
                            LocalTime.of(10, 0),
                            LocalTime.of(11, 30)))));

            sectionService.getSections();
            verify(sectionRepository, times(2)).findAll(any(Sort.class));
        }
    }

    private static Course course(Long id, String code, String name) {
        Course course = new Course();
        course.setId(id);
        course.setCourseCode(code);
        course.setName(name);
        course.setCredits(4);
        course.setActive(true);
        return course;
    }

    private static AcademicPeriod academicPeriod(Long id, String name) {
        AcademicPeriod academicPeriod = new AcademicPeriod();
        academicPeriod.setId(id);
        academicPeriod.setName(name);
        academicPeriod.setActive(true);
        return academicPeriod;
    }

    private static Section section(Long id, String code, Course course, AcademicPeriod period) {
        Section section = new Section();
        section.setId(id);
        section.setSectionCode(code);
        section.setCapacity(30);
        section.setActive(true);
        section.setCourse(course);
        section.setAcademicPeriod(period);
        section.setScheduleBlocks(List.of());
        return section;
    }

    @Configuration(proxyBeanMethods = false)
    @EnableCaching
    static class CourseCachingTestConfiguration {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(CatalogCacheNames.COURSES);
        }

        @Bean
        CourseRepository courseRepository() {
            return mock(CourseRepository.class);
        }

        @Bean
        CourseService courseService(CourseRepository courseRepository) {
            return new CourseService(courseRepository);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableCaching
    static class AcademicPeriodCachingTestConfiguration {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(CatalogCacheNames.ACADEMIC_PERIODS);
        }

        @Bean
        AcademicPeriodRepository academicPeriodRepository() {
            return mock(AcademicPeriodRepository.class);
        }

        @Bean
        AcademicPeriodService academicPeriodService(AcademicPeriodRepository academicPeriodRepository) {
            return new AcademicPeriodService(academicPeriodRepository);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableCaching
    static class SectionCachingTestConfiguration {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(CatalogCacheNames.SECTIONS);
        }

        @Bean
        SectionRepository sectionRepository() {
            return mock(SectionRepository.class);
        }

        @Bean
        CourseService courseService() {
            return mock(CourseService.class);
        }

        @Bean
        AcademicPeriodService academicPeriodService() {
            return mock(AcademicPeriodService.class);
        }

        @Bean
        SectionService sectionService(
                SectionRepository sectionRepository,
                CourseService courseService,
                AcademicPeriodService academicPeriodService) {
            return new SectionService(sectionRepository, courseService, academicPeriodService);
        }
    }
}
