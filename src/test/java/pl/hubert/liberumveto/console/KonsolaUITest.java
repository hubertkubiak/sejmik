package pl.hubert.liberumveto.console;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import org.junit.jupiter.api.Test;
import pl.hubert.liberumveto.domain.gra.GeneratorPoslow;
import pl.hubert.liberumveto.domain.gra.Gra;
import pl.hubert.liberumveto.domain.gra.KatalogUstaw;

class KonsolaUITest {

    /** Uruchamia konsolę z "nagranym" wejściem i zwraca wszystko, co wypisała. */
    private static String zagraj(String wejscie) {
        var random = new Random(2026);
        var generator = new GeneratorPoslow(List.of("Jan Zamoyski"), List.of("Jelita"), random);
        var gra = Gra.nowa(generator, KatalogUstaw.domyslny(), random);
        var bufor = new ByteArrayOutputStream();

        new KonsolaUI(new Scanner(wejscie), new PrintStream(bufor, true, StandardCharsets.UTF_8), gra).uruchom();

        return bufor.toString(StandardCharsets.UTF_8);
    }

    @Test
    void moznaRozegracPelnaGreOdStartuDoEkranuKoncowego() {
        String wyjscie = zagraj("1\nglosuj\n".repeat(Gra.LICZBA_SEJMOW));

        assertThat(wyjscie)
                .contains("=== SEJM 1/5", "=== SEJM 5/5", "Magnaci rozdają łapówki", "ZA: ")
                .contains("=== KONIEC GRY ===", "Sejm 5:");
    }

    @Test
    void bledyGraczaNiePrzerywajaGry() {
        String wyjscie = zagraj("""
                9
                abc
                1
                taniec
                przekup 999 10
                przekup 1 100000
                przekup 1 10
                posłowie
                statystyki
                pomoc
                wyjdź
                """);

        assertThat(wyjscie)
                .contains("! Wybierz ustawę od 1 do 3")
                .contains("! To nie jest poprawna liczba")
                .contains("! Nieznana komenda: taniec")
                .contains("! Nie ma posła o id 999")
                .contains("! Niewystarczający budżet")
                .contains("Jan Zamoyski h. Jelita: poparcie")
                .contains("Przewidywany głos")
                .contains("Top 3 najbardziej przekupnych", "Zagrożeni vetem")
                .contains("Dostępne komendy")
                .contains("Gra przerwana")
                .doesNotContain("KONIEC GRY");
    }

    @Test
    void koniecWejsciaKonczyGreBezWyjatku() {
        assertThat(zagraj("1\n")).contains("Gra przerwana");
    }
}
