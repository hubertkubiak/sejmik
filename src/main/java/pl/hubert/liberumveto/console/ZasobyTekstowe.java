package pl.hubert.liberumveto.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Czyta pliki tekstowe z classpath (katalog src/main/resources, po zbudowaniu wnętrze jara).
 */
public final class ZasobyTekstowe {

    private ZasobyTekstowe() {
    }

    /** Zwraca niepuste linie pliku, bez komentarzy zaczynających się od '#'. */
    public static List<String> wczytajLinie(String nazwaZasobu) {
        InputStream strumien = ZasobyTekstowe.class.getResourceAsStream("/" + nazwaZasobu);
        if (strumien == null) {
            throw new IllegalStateException("Brak zasobu na classpath: " + nazwaZasobu);
        }
        // try-with-resources: reader (a z nim strumień) zostanie zamknięty nawet przy wyjątku
        try (var reader = new BufferedReader(new InputStreamReader(strumien, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::strip)
                    .filter(linia -> !linia.isEmpty() && !linia.startsWith("#"))
                    .toList();
        } catch (IOException e) {
            // pakujemy checked IOException w unchecked: brak zasobu w jarze to błąd builda, nie gracza
            throw new UncheckedIOException("Nie udało się wczytać " + nazwaZasobu, e);
        }
    }
}
