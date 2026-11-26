package pl.hubert.sejmik.console;

import java.io.Console;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Random;
import java.util.Scanner;
import pl.hubert.sejmik.console.save.SaveFile;
import pl.hubert.sejmik.domain.game.BillCatalog;
import pl.hubert.sejmik.domain.game.DeputyGenerator;
import pl.hubert.sejmik.domain.game.Game;

/**
 * Entry point. This is the one and only place where dependencies are created and wired together
 * (the "composition root"). Every other class receives what it needs through its constructor.
 */
public final class Main {

    private static final Path SAVE_PATH = Path.of("save.json");

    private Main() {
    }

    public static void main(String[] args) {
        var random = new Random();
        var catalog = BillCatalog.defaultCatalog();
        var generator = new DeputyGenerator(
                TextResources.readLines("names.txt"),
                TextResources.readLines("coats-of-arms.txt"),
                random);

        var input = new Scanner(System.in, consoleCharset());
        var ui = new ConsoleUI(input, System.out, new SaveFile(SAVE_PATH));

        // lambdas as "recipes" for a game: the console decides which one to use
        ui.run(
                () -> Game.start(generator, catalog, random),
                state -> new Game(state, catalog, random));
    }

    /** On Windows the console often does not use UTF-8, so we ask it for its charset. */
    private static Charset consoleCharset() {
        Console console = System.console();
        return console != null ? console.charset() : Charset.defaultCharset();
    }
}
