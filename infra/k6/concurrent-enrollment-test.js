import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

const enrollmentServiceUrl = __ENV.ENROLLMENT_SERVICE_URL || 'http://localhost:8083';
const testStudentId = Number(__ENV.TEST_STUDENT_ID || 1);
const testSectionId = Number(__ENV.TEST_SECTION_ID || 1);
const VUS = Number(__ENV.VUS || 20);
const ITERATIONS = Number(__ENV.ITERATIONS || 20);
const MAX_DURATION = __ENV.MAX_DURATION || '1m';

const createdResponses = new Counter('enrollment_created_responses');
const conflictResponses = new Counter('enrollment_conflict_responses');
const unexpectedResponses = new Counter('enrollment_unexpected_responses');

export const options = {
  scenarios: {
    concurrent_enrollment: {
      executor: 'shared-iterations',
      vus: VUS,
      iterations: ITERATIONS,
      maxDuration: MAX_DURATION,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.10'],
    http_req_duration: ['p(95)<2000', 'p(99)<3000'],
    checks: ['rate>0.90'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(95)', 'p(99)', 'count'],
};

export default function () {
  // This test targets the critical concurrency rule for the checkpoint:
  // the same student should not obtain duplicate active enrollments for the same section.
  //
  // Expected data prerequisites:
  // - TEST_STUDENT_ID must already exist
  // - TEST_SECTION_ID must already exist
  //
  // Acceptable outcomes:
  // - one HTTP 201 plus multiple HTTP 409 responses
  // - or all HTTP 409 responses if an active enrollment already exists before the test begins
  const payload = JSON.stringify({
    studentId: testStudentId,
    sectionId: testSectionId,
  });

  const response = http.post(`${enrollmentServiceUrl}/api/enrollments`, payload, {
    headers: {
      'Content-Type': 'application/json',
    },
    tags: {
      scenario: 'concurrent-enrollment',
      endpoint: '/api/enrollments',
    },
  });

  if (response.status === 201) {
    createdResponses.add(1);
  } else if (response.status === 409) {
    conflictResponses.add(1);
  } else {
    unexpectedResponses.add(1);
  }

  check(response, {
    'response is 201 or 409': (r) => r.status === 201 || r.status === 409,
    '201 responses include enrollment payload': (r) => {
      if (r.status !== 201) {
        return true;
      }

      const body = r.json();
      return body && body.studentId === testStudentId && body.sectionId === testSectionId;
    },
    '409 responses communicate duplicate enrollment protection': (r) => {
      if (r.status !== 409) {
        return true;
      }

      try {
        const body = r.json();
        return typeof body.message === 'string' && body.message.length > 0;
      } catch (error) {
        return false;
      }
    },
  });

  if (__ITER === 0) {
    console.log(
      `Concurrent enrollment attempt for studentId=${testStudentId}, sectionId=${testSectionId}`
    );
  }
}
