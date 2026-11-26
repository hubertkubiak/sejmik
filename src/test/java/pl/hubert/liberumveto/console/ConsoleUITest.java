package pl.hubert.liberumveto.console;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.hubert.liberumveto.console.save.SaveFile;
import pl.hubert.liberumveto.domain.game.BillCatalog;
import pl.hubert.liberumveto.domain.game.DeputyGenerator;
import pl.hubert.liberumveto.domain.game.Game;

class ConsoleUITest {

    @TempDir
    Path directory;

    /** Runs the console with "recorded" input and returns everything it printed. */
    private String play(String input) {
        var random = new Random(2026);
        var generator = new DeputyGenerator(List.of("Jan Zamoyski"), List.of("Jelita"), random);
        var buffer = new ByteArrayOutputStream();
        var saveFile = new SaveFile(directory.resolve("save.json"));

        new ConsoleUI(new Scanner(input), new PrintStream(buffer, true, StandardCharsets.UTF_8), saveFile)
                .run(() -> Game.start(generator, BillCatalog.defaultCatalog(), random),
                        state -> new Game(state, BillCatalog.defaultCatalog(), random));

        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void fullGameCanBePlayedFromStartToGameOverScreen() {
        String output = play("1\nglosuj\n".repeat(Game.SEJM_COUNT));

        assertThat(output)
                .contains("=== SEJM 1/5", "=== SEJM 5/5", "Magnaci rozdają łapówki", "ZA: ")
                .contains("=== KONIEC GRY ===", "Sejm 5:")
                .contains("(gra zapisana w");
    }

    @Test
    void gameSavedMidwayLoadsAndContinues() {
        String first = play("""
                1
                glosuj
                2
                zapisz
                wyjdz
                """);
        assertThat(first).contains("=== SEJM 2/5");
        assertThat(directory.resolve("save.json")).exists();

        // on the second run: 1 = load, and the bill for Sejm 2 is already selected
        String second = play("1\nglosuj\n" + "1\nglosuj\n".repeat(3));

        assertThat(second)
                .contains("Znaleziono zapis gry", "Wczytano grę (sejm 2/5)")
                .doesNotContain("=== SEJM 1/5")
                .contains("=== KONIEC GRY ===");
    }

    @Test
    void playerMistakesDoNotEndTheGame() {
        String output = play("""
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

        assertThat(output)
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
    void endOfInputEndsTheGameWithoutException() {
        assertThat(play("1\n")).contains("Gra przerwana");
    }
}
