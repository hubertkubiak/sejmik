package pl.hubert.sejmik.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest
@AutoConfigureMockMvc
class WebConfigTest {

    @Autowired
    private MockMvcTester mvc;

    /** What a browser sends before a cross-origin PUT with a JSON body. */
    @Test
    void preflightFromAngularDevServerIsAllowed() {
        MvcTestResult result = mvc.options().uri("/api/games/any/selected-bill")
                .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PUT")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type")
                .exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).headers()
                .hasValue(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200");
    }

    @Test
    void locationHeaderIsExposedToJavaScript() {
        MvcTestResult result = mvc.post().uri("/api/games")
                .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                .exchange();

        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertThat(result.getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS)).contains("Location");
    }

    @Test
    void otherOriginsAreRejected() {
        assertThat(mvc.options().uri("/api/games")
                .header(HttpHeaders.ORIGIN, "http://evil.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void openApiDescriptionListsTheGameEndpoints() {
        MvcTestResult result = mvc.get().uri("/v3/api-docs").exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.info.title").isEqualTo("Sejmik API");
        assertThat(result).bodyJson().extractingPath("$.paths['/api/games/{id}/bribes'].post.summary")
                .isEqualTo("The King bribes a deputy");
    }

    @Test
    void swaggerUiIsServed() {
        assertThat(mvc.get().uri("/swagger-ui/index.html")).hasStatusOk();
    }
}
