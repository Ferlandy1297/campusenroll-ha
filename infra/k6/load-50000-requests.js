import http from 'k6/http';
import { check, group, sleep } from 'k6';

const REQUEST_TARGET = Number(__ENV.REQUEST_TARGET || 50000);
const VUS = Number(__ENV.VUS || 100);
const DURATION = __ENV.DURATION || '5m';

const studentServiceUrl = __ENV.STUDENT_SERVICE_URL || 'http://localhost:8081';
const courseServiceUrl = __ENV.COURSE_SERVICE_URL || 'http://localhost:8082';
const enrollmentServiceUrl = __ENV.ENROLLMENT_SERVICE_URL || 'http://localhost:8083';
const billingServiceUrl = __ENV.BILLING_SERVICE_URL || 'http://localhost:8084';

const requestPlan = [
  { name: 'student-health', method: 'GET', url: `${studentServiceUrl}/health` },
  { name: 'course-health', method: 'GET', url: `${courseServiceUrl}/health` },
  { name: 'enrollment-health', method: 'GET', url: `${enrollmentServiceUrl}/health` },
  { name: 'billing-health', method: 'GET', url: `${billingServiceUrl}/health` },
  { name: 'students-list', method: 'GET', url: `${studentServiceUrl}/api/students` },
  { name: 'courses-list', method: 'GET', url: `${courseServiceUrl}/api/courses` },
  { name: 'periods-list', method: 'GET', url: `${courseServiceUrl}/api/periods` },
  { name: 'sections-list', method: 'GET', url: `${courseServiceUrl}/api/sections` },
  { name: 'enrollments-list', method: 'GET', url: `${enrollmentServiceUrl}/api/enrollments` },
  { name: 'billings-list', method: 'GET', url: `${billingServiceUrl}/api/billings` },
];

// 50,000-request target calculation:
// - each VU repeats the default function until the configured duration ends
// - each iteration sends exactly requestPlan.length requests
// - with the default plan, each iteration sends 10 requests
// - therefore 5,000 iterations across all VUs would produce 50,000 total requests
// - the exact final total depends on achieved iteration throughput during the run
export const options = {
  vus: VUS,
  duration: DURATION,
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<1500', 'p(99)<2500'],
    checks: ['rate>0.95'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(95)', 'p(99)', 'count'],
};

export function setup() {
  return {
    requestTarget: REQUEST_TARGET,
    requestsPerIteration: requestPlan.length,
  };
}

export default function (data) {
  // This scenario is intentionally read-focused and non-destructive by default.
  // It exercises currently implemented GET endpoints that should exist in the checkpoint state.
  group('CampusEnroll HA read load', () => {
    for (const step of requestPlan) {
      const response = http.request(step.method, step.url, null, {
        tags: {
          scenario: 'load-50000',
          endpoint_name: step.name,
        },
      });

      check(response, {
        [`${step.name} returned a non-error status`]: (r) => r.status >= 200 && r.status < 400,
      });
    }
  });

  // Light pacing keeps the script adjustable and easier to reason about during review.
  sleep(0.2);

  // k6 does not enforce the request target automatically in this script.
  // The summary output should be reviewed after execution to confirm whether the 50,000 total was reached.
  if (__ITER === 0 && __VU === 1) {
    console.log(
      `Target reference: ${data.requestTarget} total requests, ${data.requestsPerIteration} requests per iteration.`
    );
  }
}
