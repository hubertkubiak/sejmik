package pl.hubert.liberumveto.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/**
 * Plays whole turns through the REST API, the way a frontend would.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GameFlowTest {

    @Autowired
    private MockMvcTester mvc;

    private String gameUrl;

    @BeforeEach
    void createGame() {
        gameUrl = mvc.post().uri("/api/games").exchange().getResponse().getHeader("Location");
    }

    private String body(MvcTestResult result) throws Exception {
        return result.getResponse().getContentAsString();
    }

    private MvcTestResult putJson(String url, String json) {
        return mvc.put().uri(url).contentType(MediaType.APPLICATION_JSON).content(json).exchange();
    }

    private MvcTestResult postJson(String url, String json) {
        return mvc.post().uri(url).contentType(MediaType.APPLICATION_JSON).content(json).exchange();
    }

    /** Selects the first bill on the list and returns its id. */
    private int selectFirstBill() throws Exception {
        int billId = JsonPath.read(body(mvc.get().uri(gameUrl + "/bills").exchange()), "$[0].id");
        assertThat(putJson(gameUrl + "/selected-bill", "{\"billId\": " + billId + "}")).hasStatusOk();
        return billId;
    }

    @Test
    void listsThreeBillsAndTwentyDeputiesWithoutSupportBeforeSelection() {
        assertThat(mvc.get().uri(gameUrl + "/bills")).hasStatusOk()
                .bodyJson().extractingPath("$.length()").isEqualTo(3);

        assertThat(mvc.get().uri(gameUrl + "/deputies")).hasStatusOk()
                .bodyJson().extractingPath("$.length()").isEqualTo(20);
        assertThat(mvc.get().uri(gameUrl + "/deputies")).bodyJson()
                .extractingPath("$[0].support").isNull();
    }

    @Test
    void filtersDeputiesByFactionCaseInsensitively() throws Exception {
        List<String> factions = JsonPath.read(
                body(mvc.get().uri(gameUrl + "/deputies?faction=royal").exchange()), "$[*].faction");

        assertThat(factions).isNotEmpty().containsOnly("ROYAL");
    }

    @Test
    void selectingBillTriggersMagnatesAndEnablesSortingBySupport() throws Exception {
        int billId = selectFirstBill();

        assertThat(mvc.get().uri(gameUrl)).bodyJson().extractingPath("$.selectedBill.id").isEqualTo(billId);

        List<Integer> support = JsonPath.read(
                body(mvc.get().uri(gameUrl + "/deputies?sort=support").exchange()), "$[*].support");
        assertThat(support).hasSize(20).isSorted();
    }

    @Test
    void bribeRaisesSupportAndSpendsTheKingsDucats() throws Exception {
        selectFirstBill();

        MvcTestResult result = postJson(gameUrl + "/bribes", "{\"deputyId\": 1, \"amount\": 30}");

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.kingBudget").isEqualTo(70);
        assertThat(result).bodyJson().extractingPath("$.deputy.id").isEqualTo(1);
        int before = JsonPath.read(body(result), "$.supportBefore");
        int after = JsonPath.read(body(result), "$.supportAfter");
        assertThat(after).isGreaterThanOrEqualTo(before);
    }

    @Test
    void fullGameOfFiveSejmsEndsWithResultAndHistory() throws Exception {
        for (int sejm = 1; sejm <= 5; sejm++) {
            selectFirstBill();

            MvcTestResult vote = mvc.post().uri(gameUrl + "/votes").exchange();

            assertThat(vote).hasStatusOk();
            assertThat(vote).bodyJson().extractingPath("$.sejm.number").isEqualTo(sejm);
            int votes = JsonPath.read(body(vote), "$.sejm.votesFor");
            votes += (int) JsonPath.read(body(vote), "$.sejm.votesAgainst");
            votes += (int) JsonPath.read(body(vote), "$.sejm.vetoes");
            assertThat(votes).isEqualTo(20);
        }

        assertThat(mvc.get().uri(gameUrl)).bodyJson().extractingPath("$.over").isEqualTo(true);
        assertThat(mvc.get().uri(gameUrl)).bodyJson().extractingPath("$.result").isNotNull();
        assertThat(mvc.get().uri(gameUrl + "/history")).hasStatusOk()
                .bodyJson().extractingPath("$.length()").isEqualTo(5);
    }
}
