import http from 'k6/http';
import exec from 'k6/execution';
import { check } from 'k6';

const REQUEST_TARGET = Number(__ENV.REQUEST_TARGET || 50000);
const VUS = Number(__ENV.VUS || 100);
const MAX_DURATION = __ENV.MAX_DURATION || '10m';

const studentServiceUrl = __ENV.STUDENT_SERVICE_URL || 'http://localhost:8081';
const courseServiceUrl = __ENV.COURSE_SERVICE_URL || 'http://localhost:8082';
const enrollmentServiceUrl = __ENV.ENROLLMENT_SERVICE_URL || 'http://localhost:8083';
const billingServiceUrl = __ENV.BILLING_SERVICE_URL || 'http://localhost:8084';
const notificationServiceUrl = __ENV.NOTIFICATION_SERVICE_URL || 'http://localhost:8085';

const requestPlan = [
  { name: 'student-health', method: 'GET', url: `${studentServiceUrl}/health` },
  { name: 'course-health', method: 'GET', url: `${courseServiceUrl}/health` },
  { name: 'enrollment-health', method: 'GET', url: `${enrollmentServiceUrl}/health` },
  { name: 'billing-health', method: 'GET', url: `${billingServiceUrl}/health` },
  { name: 'notification-health', method: 'GET', url: `${notificationServiceUrl}/health` },
  { name: 'students-list', method: 'GET', url: `${studentServiceUrl}/api/students` },
  { name: 'courses-list', method: 'GET', url: `${courseServiceUrl}/api/courses` },
  { name: 'periods-list', method: 'GET', url: `${courseServiceUrl}/api/periods` },
  { name: 'sections-list', method: 'GET', url: `${courseServiceUrl}/api/sections` },
  { name: 'enrollments-list', method: 'GET', url: `${enrollmentServiceUrl}/api/enrollments` },
  { name: 'billings-list', method: 'GET', url: `${billingServiceUrl}/api/billings` },
];

export const options = {
  scenarios: {
    read_load: {
      executor: 'shared-iterations',
      vus: VUS,
      iterations: REQUEST_TARGET,
      maxDuration: MAX_DURATION,
    },
  },
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
    endpointCount: requestPlan.length,
  };
}

export default function (data) {
  const step = requestPlan[exec.scenario.iterationInTest % requestPlan.length];
  const response = http.request(step.method, step.url, null, {
    tags: {
      scenario: 'load-50000',
      endpoint_name: step.name,
    },
  });

  check(response, {
    [`${step.name} returned a non-error status`]: (r) => r.status >= 200 && r.status < 400,
  });

  if (exec.scenario.iterationInTest === 0) {
    console.log(
      `Running exact-load scenario with ${data.requestTarget} total requests across ${data.endpointCount} read endpoints.`
    );
  }
}
