package com.campusenroll.enrollmentservice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.core.annotation.AnnotationUtils;

class EnrollmentServiceApplicationTest {

    @Test
    void shouldEnableRabbitInfrastructureForRuntimeListeners() {
        assertThat(AnnotationUtils.findAnnotation(EnrollmentServiceApplication.class, EnableRabbit.class)).isNotNull();
    }
}
