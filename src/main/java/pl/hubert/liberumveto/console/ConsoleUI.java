package pl.hubert.liberumveto.console;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import pl.hubert.liberumveto.console.save.SaveFile;
import pl.hubert.liberumveto.domain.exception.InsufficientBudgetException;
import pl.hubert.liberumveto.domain.exception.InvalidChoiceException;
import pl.hubert.liberumveto.domain.exception.SaveGameException;
import pl.hubert.liberumveto.domain.game.Game;
import pl.hubert.liberumveto.domain.game.GameState;
import pl.hubert.liberumveto.domain.game.SejmResult;
import pl.hubert.liberumveto.domain.game.Statistics;
import pl.hubert.liberumveto.domain.game.SupportChange;
import pl.hubert.liberumveto.domain.model.Bill;
import pl.hubert.liberumveto.domain.model.Deputy;
import pl.hubert.liberumveto.domain.model.Faction;
import pl.hubert.liberumveto.domain.model.Vote;
import pl.hubert.liberumveto.domain.sejm.VoteResult;

/**
 * The text interface: reads commands, calls the logic in {@link Game} and prints the results.
 * All text shown to the player is in Polish.
 * Input and output come in through the constructor, so tests can feed text instead of a keyboard.
 */
public class ConsoleUI {

    private static final String HELP = """
            Dostępne komendy:
              przekup <id> <kwota>   przekup posła (podnosi jego poparcie dla ustawy)
              posłowie  (p)          tabela posłów z aktualnym poparciem
              statystyki (s)         frakcje, najbardziej przekupni, zagrożeni vetem
              głosuj    (g)          zakończ przekupstwa i przeprowadź głosowanie
              zapisz                 zapisz grę (zapis dzieje się też po każdym sejmie)
              pomoc     (?)          ta lista
              wyjdź                  przerwij grę
            Progi: poparcie >= 50 to głos ZA, poniżej 15 grozi VETO.""";

    private final Scanner input;
    private final PrintStream output;
    private final SaveFile saveFile;
    private final CommandParser parser = new CommandParser();
    private Game game;

    public ConsoleUI(Scanner input, PrintStream output, SaveFile saveFile) {
        this.input = input;
        this.output = output;
        this.saveFile = saveFile;
    }

    /**
     * The main game loop. How to create a game is passed in as functions, so the console does not
     * need to know where deputies, the bill catalog or the random generator come from.
     *
     * @param newGame creates a new game
     * @param resume  creates a game from a loaded state
     */
    public void run(Supplier<Game> newGame, Function<GameState, Game> resume) {
        output.println("Witaj, Najjaśniejszy Panie! Przeforsuj 3 z 5 ustaw, zanim magnaci zerwą sejmy.");
        Optional<Game> started = chooseStart(newGame, resume);
        if (started.isEmpty()) {
            output.println("Bywaj, Miłościwy Panie!");
            return;
        }
        game = started.get();
        output.println("Wpisz pomoc, aby zobaczyć komendy.");
        while (!game.isOver()) {
            if (!playSejm()) {
                output.println("Gra przerwana. Bywaj, Miłościwy Panie!");
                return;
            }
        }
        showGameOver();
    }

    /** F1: a new game, or loading the save if one exists. */
    private Optional<Game> chooseStart(Supplier<Game> newGame, Function<GameState, Game> resume) {
        if (!saveFile.exists()) {
            return Optional.of(newGame.get());
        }
        while (true) {
            output.println("Znaleziono zapis gry. 1) Wczytaj zapis  2) Nowa gra");
            Optional<String> line = readLine();
            if (line.isEmpty()) {
                return Optional.empty();
            }
            switch (line.get().strip()) {
                case "1" -> {
                    try {
                        GameState state = saveFile.load();
                        output.printf("Wczytano grę (sejm %d/%d).%n", state.sejmNumber(), Game.SEJM_COUNT);
                        return Optional.of(resume.apply(state));
                    } catch (SaveGameException e) {
                        output.println("! " + e.getMessage() + ". Zaczynamy nową grę.");
                        return Optional.of(newGame.get());
                    }
                }
                case "2" -> {
                    return Optional.of(newGame.get());
                }
                default -> output.println("! Wpisz 1 albo 2");
            }
        }
    }

    /** @return false if the player wants to quit */
    private boolean playSejm() {
        var state = game.state();
        output.printf("%n=== SEJM %d/%d | Budżet króla: %d dukatów ===%n",
                state.sejmNumber(), Game.SEJM_COUNT, state.king().budget());

        if (game.selectedBill().isEmpty() && !chooseBill()) {
            return false;
        }
        Bill bill = game.selectedBill().orElseThrow();
        output.println("Obrady nad ustawą: " + bill.title());

        while (true) {
            output.println("Komenda (przekup <id> <kwota> | posłowie | statystyki | głosuj | zapisz | pomoc):");
            Optional<String> line = readLine();
            if (line.isEmpty()) {
                return false;
            }
            try {
                Command command = parser.parse(line.get());
                // pattern matching in switch (Java 21), deconstructing the Bribe record
                switch (command) {
                    case Command.Bribe(int deputyId, int amount) -> bribe(deputyId, amount);
                    case Command.ShowDeputies() -> showDeputies(bill);
                    case Command.ShowStatistics() -> showStatistics(bill);
                    case Command.Save() -> save();
                    case Command.Help() -> output.println(HELP);
                    case Command.CallVote() -> {
                        vote();
                        return true;
                    }
                    case Command.Quit() -> {
                        return false;
                    }
                }
            } catch (InvalidChoiceException | InsufficientBudgetException | IllegalArgumentException e) {
                // multi-catch: player mistakes are shown and we ask again, the game does not crash
                output.println("! " + e.getMessage());
            }
        }
    }

    private boolean chooseBill() {
        List<Bill> bills = game.availableBills();
        while (true) {
            output.println("Wybierz ustawę:");
            for (int i = 0; i < bills.size(); i++) {
                output.printf(" %d) %-42s %s%n", i + 1, bills.get(i).title(), describeImpact(bills.get(i)));
            }
            Optional<String> line = readLine();
            if (line.isEmpty() || CommandParser.normalize(line.get().strip()).equals("wyjdz")) {
                return false;
            }
            try {
                Map<Deputy, Integer> bribes = game.selectBill(parser.parseNumber(line.get()));
                output.printf("Magnaci rozdają łapówki... (przekupionych posłów: %d)%n", bribes.size());
                return true;
            } catch (InvalidChoiceException e) {
                output.println("! " + e.getMessage());
            }
        }
    }

    /** E.g. "[królewska +20, magnacka -30, niezależna +0]". */
    private static String describeImpact(Bill bill) {
        return Arrays.stream(Faction.values())
                .map(faction -> "%s %+d".formatted(faction.displayName(), bill.impactOn(faction)))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private void bribe(int deputyId, int amount) {
        SupportChange change = game.bribe(deputyId, amount);
        output.printf("%s: poparcie %d → %d (zostało %d dukatów)%n",
                change.deputy().fullName(), change.before(), change.after(), game.state().king().budget());
    }

    private void showDeputies(Bill bill) {
        output.printf("%3s  %-36s %-11s %5s %5s %8s  %s%n",
                "ID", "Poseł", "Frakcja", "Lojal", "Przek", "Poparcie", "Przewidywany głos");
        game.state().deputies().stream()
                .sorted(Comparator.comparingInt((Deputy d) -> d.calculateSupport(bill)))
                .forEach(deputy -> {
                    int support = deputy.calculateSupport(bill);
                    output.printf("%3d  %-36s %-11s %5d %5.2f %8d  %s%n",
                            deputy.id(), deputy.fullName(), deputy.faction().displayName(),
                            deputy.loyaltyToKing(), deputy.corruptibility(), support, describeVote(support));
                });
    }

    private void showStatistics(Bill bill) {
        var statistics = new Statistics(game.state().deputies());
        Map<Faction, Long> sizes = statistics.factionSizes();

        output.println("Średnia lojalność wobec Króla:");
        statistics.averageLoyaltyByFaction().forEach((faction, average) ->
                output.printf("  %-11s %5.1f  (posłów: %d)%n", faction.displayName(), average, sizes.get(faction)));

        output.printf("Średnie poparcie dla ustawy: %.1f%n", statistics.averageSupport(bill));

        output.println("Top 3 najbardziej przekupnych:");
        statistics.mostCorruptible(3).forEach(deputy ->
                output.printf("  [%d] %s (%.2f)%n", deputy.id(), deputy.fullName(), deputy.corruptibility()));

        List<Deputy> atRisk = statistics.atRiskOfVeto(bill);
        output.printf("Zagrożeni vetem (%d):%n", atRisk.size());
        atRisk.forEach(deputy ->
                output.printf("  [%d] %s: poparcie %d%n", deputy.id(), deputy.fullName(), deputy.calculateSupport(bill)));
    }

    private static String describeVote(int support) {
        if (support >= Deputy.FOR_THRESHOLD) {
            return "ZA";
        }
        if (support >= Deputy.VETO_THRESHOLD) {
            return "PRZECIW";
        }
        int chance = (Deputy.VETO_THRESHOLD - support) * 100 / Deputy.VETO_THRESHOLD;
        return "!!! VETO (" + chance + "%)";
    }

    private void vote() {
        SejmResult sejmResult = game.vote();
        VoteResult result = sejmResult.result();
        output.printf("ZA: %d | PRZECIW: %d | VETO: %d%n",
                result.count(Vote.FOR), result.count(Vote.AGAINST), result.count(Vote.VETO));
        output.println(describeOutcome(result));
        save(); // F8: autosave after every turn
    }

    private void save() {
        try {
            saveFile.save(game.state());
            output.println("(gra zapisana w " + saveFile.path() + ")");
        } catch (SaveGameException e) {
            // a failed save should not end the game; tell the player and carry on
            output.println("! " + e.getMessage() + ": " + e.getCause().getMessage());
        }
    }

    private static String describeOutcome(VoteResult result) {
        return result.vetoer()
                .map(deputy -> "!!! Sejm zerwany przez: " + deputy.fullName())
                .orElse(result.passed() ? "Vivat! Ustawa przyjęta." : "Ustawa odrzucona większością głosów.");
    }

    private void showGameOver() {
        output.println();
        output.println("=== KONIEC GRY ===");
        for (SejmResult r : game.state().history()) {
            output.printf("Sejm %d: %-40s %s%n", r.number(), r.bill().title(), shortOutcome(r.result()));
        }
        output.printf("Przyjęte: %d | Zerwane: %d%n", game.passedCount(), game.dissolvedCount());
        output.println(game.result().description());
    }

    private static String shortOutcome(VoteResult result) {
        if (result.dissolved()) {
            return "ZERWANY (" + result.vetoer().orElseThrow().fullName() + ")";
        }
        return result.passed() ? "PRZYJĘTA" : "ODRZUCONA";
    }

    /** An empty value means the end of input (e.g. Ctrl+D, or the end of the text in tests). */
    private Optional<String> readLine() {
        output.print("> ");
        output.flush();
        return input.hasNextLine() ? Optional.of(input.nextLine()) : Optional.empty();
    }
}
