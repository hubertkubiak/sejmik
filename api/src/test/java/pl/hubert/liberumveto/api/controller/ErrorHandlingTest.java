package pl.hubert.liberumveto.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/**
 * Every kind of error comes back as application/problem+json with a fitting status code.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ErrorHandlingTest {

    @Autowired
    private MockMvcTester mvc;

    private String gameUrl;

    @BeforeEach
    void createGame() {
        gameUrl = mvc.post().uri("/api/games").exchange().getResponse().getHeader("Location");
    }

    private MvcTestResult postJson(String url, String json) {
        return mvc.post().uri(url).contentType(MediaType.APPLICATION_JSON).content(json).exchange();
    }

    private MvcTestResult putJson(String url, String json) {
        return mvc.put().uri(url).contentType(MediaType.APPLICATION_JSON).content(json).exchange();
    }

    private void selectFirstBill() throws Exception {
        String bills = mvc.get().uri(gameUrl + "/bills").exchange().getResponse().getContentAsString();
        int billId = JsonPath.read(bills, "$[0].id");
        assertThat(putJson(gameUrl + "/selected-bill", "{\"billId\": " + billId + "}")).hasStatusOk();
    }

    @Test
    void unknownGameIs404ProblemDetail() {
        MvcTestResult result = mvc.get().uri("/api/games/00000000-0000-0000-0000-000000000000").exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath("$.title").isEqualTo("Game not found");
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo(404);
        assertThat(result).bodyJson().extractingPath("$.detail").asString().startsWith("Nie ma gry o id");
    }

    @Test
    void invalidBribeBodyReportsEveryField() {
        MvcTestResult result = postJson(gameUrl + "/bribes", "{\"amount\": 0}");

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.title").isEqualTo("Validation failed");
        assertThat(result).bodyJson().extractingPath("$.errors.deputyId").isEqualTo("Podaj id posła");
        assertThat(result).bodyJson().extractingPath("$.errors.amount").isEqualTo("Kwota łapówki musi być dodatnia");
    }

    @Test
    void malformedJsonIs400() {
        assertThat(postJson(gameUrl + "/bribes", "{ to nie json")).hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void bribeBeforeSelectingBillIsInvalidChoice() {
        MvcTestResult result = postJson(gameUrl + "/bribes", "{\"deputyId\": 1, \"amount\": 10}");

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("Najpierw wybierz ustawę");
    }

    @Test
    void sortingBySupportWithoutBillIsInvalidChoice() {
        assertThat(mvc.get().uri(gameUrl + "/deputies?sort=support")).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void unknownSortValueIs400() {
        assertThat(mvc.get().uri(gameUrl + "/deputies?sort=height")).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void bribeOverBudgetIs422WithAmounts() throws Exception {
        selectFirstBill();

        MvcTestResult result = postJson(gameUrl + "/bribes", "{\"deputyId\": 1, \"amount\": 1000}");

        assertThat(result).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(result).bodyJson().extractingPath("$.budget").isEqualTo(100);
        assertThat(result).bodyJson().extractingPath("$.amount").isEqualTo(1000);
    }

    @Test
    void unknownDeputyIs400() throws Exception {
        selectFirstBill();

        assertThat(postJson(gameUrl + "/bribes", "{\"deputyId\": 99, \"amount\": 10}"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.detail").isEqualTo("Nie ma posła o id 99");
    }

    @Test
    void selectingBillTwiceIs409() throws Exception {
        selectFirstBill();

        String bills = mvc.get().uri(gameUrl + "/bills").exchange().getResponse().getContentAsString();
        int billId = JsonPath.read(bills, "$[1].id");

        assertThat(putJson(gameUrl + "/selected-bill", "{\"billId\": " + billId + "}"))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson().extractingPath("$.detail").isEqualTo("Ustawa na ten sejm została już wybrana");
    }

    @Test
    void billNotOnTheListIs400() {
        assertThat(putJson(gameUrl + "/selected-bill", "{\"billId\": 999}")).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void wrongHttpMethodIs405() {
        assertThat(mvc.delete().uri(gameUrl)).hasStatus(HttpStatus.METHOD_NOT_ALLOWED);
    }
}
