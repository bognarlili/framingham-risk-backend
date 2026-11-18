package hu.framingham.risk.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.framingham.risk.dto.request.RiskCaseRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RiskCaseIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.10-alpine")
            .withDatabaseName("framingham_test").withUsername("test").withPassword("test");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void createReadAndListWithRealPostgres() throws Exception {
        RiskCaseRequest request = new RiskCaseRequest(1, 48, 2, 0, 0, 0, 0, 0, 0,
                210.0, 125.0, 82.0, 27.3, 71.0, 91.0);
        String saved = mvc.perform(post("/api/cases").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.age", is(48)))
                .andReturn().getResponse().getContentAsString();
        String id = json.readTree(saved).get("id").asText();

        mvc.perform(get("/api/cases/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.totChol", is(210.0)));
        mvc.perform(get("/api/cases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)));
        mvc.perform(post("/api/predictions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseId\":\"" + id + "\",\"modelKey\":\"logistic-regression-full\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status", is(503)));
        mvc.perform(get("/api/predictions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test
    void rejectInvalidAge() throws Exception {
        mvc.perform(post("/api/cases").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"age\":-10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.age").exists());
    }
}
