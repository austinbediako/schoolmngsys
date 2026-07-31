package com.drakalabs.schoolmngsys.shared.web.error;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies every response follows the RFC 7807 contract in docs/10 §2: stable {@code type} slug,
 * {@code traceId}, {@code errors[]} for validation, {@code ruleId} for business-rule rejections
 * (docs/14 §5 WP-0 test plan: "problem-format contract test"). Security is disabled here — auth
 * is WP-1's concern, not WP-0's.
 */
@WebMvcTest(controllers = ProblemContractTestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ProblemFormatContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void notFoundReturnsProblemJsonWithCatalogType() throws Exception {
        mockMvc
                .perform(get("/__test/problems/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type", is("https://ubs-lmis.example/problems/not-found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    void ruleViolationCarriesRuleId() throws Exception {
        mockMvc
                .perform(get("/__test/problems/rule-violation"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type", is("https://ubs-lmis.example/problems/rule-violation")))
                .andExpect(jsonPath("$.ruleId", is("BR-EN-001")));
    }

    @Test
    void validationFailureListsFieldErrors() throws Exception {
        mockMvc
                .perform(
                        post("/__test/problems/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type", is("https://ubs-lmis.example/problems/validation")))
                .andExpect(jsonPath("$.errors[0].field", is("name")))
                .andExpect(jsonPath("$.traceId").exists());
    }
}
