package pl.hubert.sejmik.api.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import pl.hubert.sejmik.domain.game.BillCatalog;
import pl.hubert.sejmik.domain.game.DeputyGenerator;

/**
 * Teaches Spring how to build the domain objects. The domain has no Spring annotations,
 * so instead of {@code @Component} on those classes we declare them here as {@code @Bean} methods.
 * This is the Spring counterpart of the console's {@code Main}: the composition root.
 */
@Configuration
public class GameConfig {

    @Bean
    Random random() {
        return new Random();
    }

    @Bean
    BillCatalog billCatalog() {
        return BillCatalog.defaultCatalog();
    }

    /** Spring sees the {@code Random} parameter and passes in the bean defined above. */
    @Bean
    DeputyGenerator deputyGenerator(Random random) {
        return new DeputyGenerator(readLines("names.txt"), readLines("coats-of-arms.txt"), random);
    }

    private static List<String> readLines(String resourceName) {
        try {
            return new ClassPathResource(resourceName).getContentAsString(StandardCharsets.UTF_8)
                    .lines()
                    .map(String::strip)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + resourceName, e);
        }
    }
}
