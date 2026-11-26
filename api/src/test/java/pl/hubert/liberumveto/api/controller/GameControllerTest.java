package pl.hubert.liberumveto.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/**
 * Starts the whole Spring context (without a real HTTP server) and sends requests through MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTest {

    /** Field injection is fine in tests: JUnit creates the test instance, Spring fills in the field. */
    @Autowired
    private MockMvcTester mvc;

    @Test
    void createsNewGame() {
        MvcTestResult result = mvc.post().uri("/api/games").exchange();

        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertThat(result).headers().hasHeaderSatisfying("Location",
                values -> assertThat(values.getFirst()).matches(".*/api/games/[0-9a-f-]{36}"));
        assertThat(result).bodyJson().extractingPath("$.sejmNumber").isEqualTo(1);
        assertThat(result).bodyJson().extractingPath("$.sejmCount").isEqualTo(5);
        assertThat(result).bodyJson().extractingPath("$.kingBudget").isEqualTo(100);
        assertThat(result).bodyJson().extractingPath("$.over").isEqualTo(false);
        assertThat(result).bodyJson().extractingPath("$.selectedBill").isNull();
    }

    @Test
    void returnsCreatedGameById() {
        String location = mvc.post().uri("/api/games").exchange().getResponse().getHeader("Location");

        MvcTestResult result = mvc.get().uri(location).exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.id").isEqualTo(location.substring(location.lastIndexOf('/') + 1));
    }

    @Test
    void unknownGameReturns404() {
        assertThat(mvc.get().uri("/api/games/00000000-0000-0000-0000-000000000000"))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void malformedIdReturns400() {
        assertThat(mvc.get().uri("/api/games/not-a-uuid")).hasStatus(HttpStatus.BAD_REQUEST);
    }
}
