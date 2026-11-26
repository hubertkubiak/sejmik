package pl.hubert.liberumveto.console.save;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.hubert.liberumveto.domain.exception.SaveGameException;
import pl.hubert.liberumveto.domain.game.BillCatalog;
import pl.hubert.liberumveto.domain.game.DeputyGenerator;
import pl.hubert.liberumveto.domain.game.Game;
import pl.hubert.liberumveto.domain.game.GameState;

class SaveFileTest {

    /** JUnit creates an empty directory before the test and cleans it up afterwards. */
    @TempDir
    Path directory;

    /** A game in progress: two Sejms in the history, the third with a selected bill and bribes. */
    private static Game gameInProgress() {
        var random = new Random(77);
        var game = Game.start(new DeputyGenerator(List.of("Jan Zamoyski", "Michał Pac"),
                List.of("Jelita", "Gozdawa"), random), BillCatalog.defaultCatalog(), random);
        for (int i = 0; i < 2; i++) {
            game.selectBill(1);
            game.bribe(1, 10);
            game.vote();
        }
        game.selectBill(2);
        game.bribe(3, 25);
        return game;
    }

    @Test
    void saveAndLoadGiveIdenticalState() throws SaveGameException {
        GameState original = gameInProgress().state();
        var saveFile = new SaveFile(directory.resolve("save.json"));

        saveFile.save(original);
        GameState loaded = saveFile.load();

        // field-by-field recursive comparison, including private fields and the history
        assertThat(loaded).usingRecursiveComparison().isEqualTo(original);
        assertThat(loaded.history()).hasSize(2);
        assertThat(loaded.deputy(3).kingBribes()).isEqualTo(25);
    }

    @Test
    void loadedGameCanBeFinished() throws SaveGameException {
        var saveFile = new SaveFile(directory.resolve("save.json"));
        saveFile.save(gameInProgress().state());

        var game = new Game(saveFile.load(), BillCatalog.defaultCatalog(), new Random(1));
        game.vote();
        while (!game.isOver()) {
            game.selectBill(1);
            game.vote();
        }

        assertThat(game.state().history()).hasSize(Game.SEJM_COUNT);
        assertThat(game.result()).isNotNull();
    }

    @Test
    void saveIsReadableJson() throws Exception {
        Path path = directory.resolve("save.json");
        new SaveFile(path).save(gameInProgress().state());

        String json = Files.readString(path);

        assertThat(json).contains("\"version\" : 1", "\"sejmNumber\" : 3", "\"deputies\"", "\"MAGNATE\"");
        try (var files = Files.list(directory)) {
            assertThat(files).containsExactly(path); // no orphaned temporary files
        }
    }

    @Test
    void missingFileIsASaveError() {
        var saveFile = new SaveFile(directory.resolve("missing.json"));

        assertThat(saveFile.exists()).isFalse();
        assertThatThrownBy(saveFile::load)
                .isInstanceOf(SaveGameException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void corruptedFileIsASaveError() throws IOException {
        Path path = directory.resolve("save.json");
        Files.writeString(path, "{ this is not json");

        assertThatThrownBy(() -> new SaveFile(path).load())
                .isInstanceOf(SaveGameException.class)
                .hasMessageContaining("uszkodzony");
    }
}
