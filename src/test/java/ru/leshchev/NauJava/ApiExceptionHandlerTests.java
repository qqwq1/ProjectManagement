package ru.leshchev.NauJava;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.leshchev.NauJava.web.error.ApiExceptionHandler;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;

class ApiExceptionHandlerTests {
    @RestController
    static class FailingController {
        @GetMapping("/failure")
        String fail() {
            throw new IllegalStateException("private database details");
        }
    }

    @Test
    void unexpectedErrorsReturn500WithoutLeakingExceptionDetails() throws Exception {
        MockMvcBuilders.standaloneSetup(new FailingController())
                .setControllerAdvice(new ApiExceptionHandler()).build()
                .perform(get("/failure"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Internal server error"))
                .andExpect(jsonPath("$.path").value("/failure"))
                .andExpect(content().string(not(containsString("private database details"))));
    }
}
