package pl.hubert.sejmik.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Reads text files from the classpath (src/main/resources, i.e. the inside of the jar once built).
 */
public final class TextResources {

    private TextResources() {
    }

    /** Returns the non-empty lines of the file, skipping comments that start with '#'. */
    public static List<String> readLines(String resourceName) {
        InputStream stream = TextResources.class.getResourceAsStream("/" + resourceName);
        if (stream == null) {
            throw new IllegalStateException("Resource not found on the classpath: " + resourceName);
        }
        // try-with-resources: the reader (and the stream with it) is closed even if an exception occurs
        try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::strip)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .toList();
        } catch (IOException e) {
            // wrap the checked IOException in an unchecked one: a missing resource is a build error,
            // not something the player can fix
            throw new UncheckedIOException("Could not read " + resourceName, e);
        }
    }
}
