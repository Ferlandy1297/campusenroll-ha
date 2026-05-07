import http from 'k6/http';
import { check, group, sleep } from 'k6';

const services = [
  {
    name: 'student-service',
    baseUrl: __ENV.STUDENT_SERVICE_URL || 'http://localhost:8081',
  },
  {
    name: 'course-service',
    baseUrl: __ENV.COURSE_SERVICE_URL || 'http://localhost:8082',
  },
  {
    name: 'enrollment-service',
    baseUrl: __ENV.ENROLLMENT_SERVICE_URL || 'http://localhost:8083',
  },
  {
    name: 'billing-service',
    baseUrl: __ENV.BILLING_SERVICE_URL || 'http://localhost:8084',
  },
  {
    name: 'notification',
    baseUrl: __ENV.NOTIFICATION_SERVICE_URL || 'http://localhost:8085',
  },
];

export const options = {
  vus: 1,
  iterations: 1,
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<1000', 'max<3000'],
    checks: ['rate>0.95'],
  },
};

export default function () {
  // This smoke test validates only current service availability.
  // It is intentionally lightweight so it can be executed before broader validation.
  group('CampusEnroll HA health endpoints', () => {
    for (const service of services) {
      const url = `${service.baseUrl}/health`;
      const response = http.get(url, {
        tags: {
          scenario: 'smoke',
          service: service.name,
          endpoint: '/health',
        },
      });

      check(response, {
        [`${service.name} responded with HTTP 200`]: (r) => r.status === 200,
        [`${service.name} health body is not empty`]: (r) => r.body && r.body.length > 0,
      });
    }
  });

  sleep(1);
}
