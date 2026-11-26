package pl.hubert.liberumveto.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the REST API.
 *
 * <p>{@code @SpringBootApplication} turns on three things: configuration in this class,
 * auto-configuration (e.g. "Spring MVC is on the classpath, so start Tomcat") and component scanning
 * of this package and its subpackages ({@code controller}, {@code service}, ...).
 */
@SpringBootApplication
public class LiberumVetoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiberumVetoApiApplication.class, args);
    }
}
