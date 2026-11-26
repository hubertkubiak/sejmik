package pl.hubert.sejmik.api.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC customisation. {@code WebMvcConfigurer} is a set of "hooks": we override only what we
 * want to change and keep the rest of Spring Boot's auto-configuration.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final List<String> allowedOrigins;

    /** {@code @Value} injects a property from application.properties; Spring splits "a,b" into a list. */
    public WebConfig(@Value("${sejmik.cors.allowed-origins}") List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * By default Spring MVC converts "ROYAL" to {@code Faction.ROYAL} but rejects "royal".
     * Spring Boot's application converters include a lenient String-to-enum converter
     * (case-insensitive, ignores '-' and '_'), so {@code ?faction=royal&sort=support} works too.
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        ApplicationConversionService.addApplicationConverters(registry);
    }

    /**
     * CORS: a browser only lets a page from http://localhost:4200 read responses from
     * http://localhost:8080 if the server says so in Access-Control-Allow-* headers. For "non-simple"
     * requests (PUT, JSON bodies) the browser first sends an OPTIONS "preflight" to ask.
     * {@code exposedHeaders}: without it, JavaScript could not read the Location header.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT")
                .exposedHeaders(HttpHeaders.LOCATION);
    }
}
