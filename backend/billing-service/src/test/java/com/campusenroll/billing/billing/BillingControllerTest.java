package com.campusenroll.billing.billing;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campusenroll.billing.billing.dto.BillingResponse;
import com.campusenroll.billing.error.ConflictException;
import com.campusenroll.billing.error.GlobalExceptionHandler;
import com.campusenroll.billing.error.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class BillingControllerTest {

    private MockMvc mockMvc;
    private StubBillingService billingService;

    @BeforeEach
    void setUp() {
        billingService = new StubBillingService();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(new BillingController(billingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void shouldListBillings() throws Exception {
        billingService.getAllResponse = List.of(new BillingResponse(
                1L,
                100L,
                new BigDecimal("150.75"),
                "USD",
                BillingStatus.PENDING,
                OffsetDateTime.parse("2026-05-05T10:15:30Z")));

        mockMvc.perform(get("/api/billings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void shouldCreateBilling() throws Exception {
        billingService.createResponse = new BillingResponse(
                1L,
                100L,
                new BigDecimal("150.75"),
                "USD",
                BillingStatus.PENDING,
                OffsetDateTime.parse("2026-05-05T10:15:30Z"));

        mockMvc.perform(post("/api/billings")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "enrollmentId": 100,
                                  "amount": 150.75,
                                  "currency": "USD",
                                  "status": "PENDING"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/billings/1"))
                .andExpect(jsonPath("$.enrollmentId").value(100))
                .andExpect(jsonPath("$.amount").value(150.75))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldRejectCreateWhenRequiredFieldsAreMissingOrInvalid() throws Exception {
        mockMvc.perform(post("/api/billings")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 0,
                                  "currency": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[?(@ == 'enrollmentId is required')]").exists())
                .andExpect(jsonPath("$.details[?(@ == 'amount must be greater than zero')]").exists())
                .andExpect(jsonPath("$.details[?(@ == 'currency is required')]").exists())
                .andExpect(jsonPath("$.details[?(@ == 'status is required')]").exists());
    }

    @Test
    void shouldReturnNotFoundForMissingBilling() throws Exception {
        billingService.getByIdException = new ResourceNotFoundException("Billing not found");

        mockMvc.perform(get("/api/billings/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Billing not found"));
    }

    @Test
    void shouldRejectInvalidStatusPayload() throws Exception {
        mockMvc.perform(patch("/api/billings/1/status")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "INVALID"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request payload"))
                .andExpect(jsonPath("$.details[0]").value("status must be valid"));
    }

    @Test
    void shouldReturnConflictForDuplicatePendingBilling() throws Exception {
        billingService.createException = new ConflictException("An active billing already exists for this enrollment");

        mockMvc.perform(post("/api/billings")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "enrollmentId": 100,
                                  "amount": 150.75,
                                  "currency": "USD",
                                  "status": "PENDING"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An active billing already exists for this enrollment"));
    }

    private static final class StubBillingService extends BillingService {

        private List<BillingResponse> getAllResponse = List.of();
        private BillingResponse createResponse;
        private RuntimeException createException;
        private RuntimeException getByIdException;

        private StubBillingService() {
            super(null);
        }

        @Override
        public List<BillingResponse> getAll() {
            return getAllResponse;
        }

        @Override
        public BillingResponse getById(Long id) {
            if (getByIdException != null) {
                throw getByIdException;
            }
            return createResponse;
        }

        @Override
        public BillingResponse create(com.campusenroll.billing.billing.dto.CreateBillingRequest request) {
            if (createException != null) {
                throw createException;
            }
            return createResponse;
        }
    }
}
