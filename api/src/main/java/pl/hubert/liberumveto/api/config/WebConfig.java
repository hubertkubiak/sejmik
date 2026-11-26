package pl.hubert.liberumveto.api.config;

import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC customisation. {@code WebMvcConfigurer} is a set of "hooks": we override only what we
 * want to change and keep the rest of Spring Boot's auto-configuration.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * By default Spring MVC converts "ROYAL" to {@code Faction.ROYAL} but rejects "royal".
     * Spring Boot's application converters include a lenient String-to-enum converter
     * (case-insensitive, ignores '-' and '_'), so {@code ?faction=royal&sort=support} works too.
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        ApplicationConversionService.addApplicationConverters(registry);
    }
}
