package pl.hubert.liberumveto.domain.game;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import pl.hubert.liberumveto.domain.exception.InvalidChoiceException;
import pl.hubert.liberumveto.domain.exception.SejmDissolvedException;
import pl.hubert.liberumveto.domain.model.Bill;
import pl.hubert.liberumveto.domain.model.Deputy;
import pl.hubert.liberumveto.domain.player.King;
import pl.hubert.liberumveto.domain.player.Magnate;
import pl.hubert.liberumveto.domain.sejm.Sejm;
import pl.hubert.liberumveto.domain.sejm.VoteResult;

/**
 * The game rules: how a turn goes and when the game ends.
 * It knows nothing about the console, so the same class will later back the REST API.
 *
 * <p>A turn: {@link #selectBill(int)} (the magnates bribe right away) → any number of
 * {@link #bribe(int, int)} calls → {@link #vote()}.
 */
public class Game {

    public static final int SEJM_COUNT = 5;
    public static final int DEPUTY_COUNT = 20;
    public static final int BILLS_TO_CHOOSE_FROM = 3;
    public static final int WINNING_THRESHOLD = 3;

    static final int KING_BUDGET = 100;
    static final int KING_BONUS_PER_PASSED_BILL = 20;
    static final int MAGNATE_BUDGET = 160;
    static final int MAGNATE_BONUS_PER_DISSOLUTION = 20;

    private final GameState state;
    private final BillCatalog catalog;
    private final Random random;

    /** Resumes a game from an existing state (e.g. one loaded from a file). */
    public Game(GameState state, BillCatalog catalog, Random random) {
        this.state = Objects.requireNonNull(state, "state");
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.random = Objects.requireNonNull(random, "random");
    }

    /** Factory method: a brand-new game with randomly generated deputies. */
    public static Game start(DeputyGenerator generator, BillCatalog catalog, Random random) {
        var state = new GameState(
                1,
                generator.generate(DEPUTY_COUNT),
                new King("Król", KING_BUDGET),
                new Magnate("Magnaci", MAGNATE_BUDGET),
                List.of(),
                catalog.draw(BILLS_TO_CHOOSE_FROM, random),
                null);
        return new Game(state, catalog, random);
    }

    public GameState state() {
        return state;
    }

    public List<Bill> availableBills() {
        return state.availableBills();
    }

    public Optional<Bill> selectedBill() {
        return state.selectedBill();
    }

    /**
     * The King picks a bill (its number on the list, starting at 1), then the magnates bribe against it.
     *
     * @return the bribes handed out by the magnates
     */
    public Map<Deputy, Integer> selectBill(int positionOnList) {
        requireGameInProgress();
        if (state.selectedBill().isPresent()) {
            throw new IllegalStateException("A bill has already been selected for this Sejm");
        }
        List<Bill> bills = state.availableBills();
        if (positionOnList < 1 || positionOnList > bills.size()) {
            throw new InvalidChoiceException("Wybierz ustawę od 1 do " + bills.size());
        }
        Bill bill = bills.get(positionOnList - 1);
        state.setSelectedBill(bill);
        return state.magnate().distributeBribes(state.deputies(), bill);
    }

    /** The King bribes a deputy; returns the support before and after the bribe. */
    public SupportChange bribe(int deputyId, int amount) {
        Bill bill = requireSelectedBill();
        Deputy deputy = state.deputy(deputyId);
        int before = deputy.calculateSupport(bill);
        state.king().bribe(deputy, amount);
        return new SupportChange(deputy, before, deputy.calculateSupport(bill));
    }

    /** Votes on the selected bill. Afterwards the game moves on to the next Sejm. */
    public SejmResult vote() {
        Bill bill = requireSelectedBill();
        VoteResult result;
        try {
            result = new Sejm(state.deputies(), random).deliberate(bill);
        } catch (SejmDissolvedException e) {
            // a dissolution is a legitimate turn outcome, so instead of rethrowing we record it
            result = e.result();
        }
        var sejmResult = new SejmResult(state.sejmNumber(), bill, result);
        state.addToHistory(sejmResult);
        if (!isOver()) {
            prepareNextSejm();
        } else {
            state.setSelectedBill(null);
        }
        return sejmResult;
    }

    private void prepareNextSejm() {
        state.nextSejm();
        state.deputies().forEach(Deputy::clearBribes);
        state.king().setBudget(KING_BUDGET + KING_BONUS_PER_PASSED_BILL * (int) passedCount());
        state.magnate().setBudget(MAGNATE_BUDGET + MAGNATE_BONUS_PER_DISSOLUTION * (int) dissolvedCount());
        state.setAvailableBills(catalog.draw(BILLS_TO_CHOOSE_FROM, random));
        state.setSelectedBill(null);
    }

    public boolean isOver() {
        return state.history().size() >= SEJM_COUNT;
    }

    public long passedCount() {
        return state.history().stream().filter(r -> r.result().passed()).count();
    }

    public long dissolvedCount() {
        return state.history().stream().filter(r -> r.result().dissolved()).count();
    }

    /** The outcome of the game; available only after the last Sejm. */
    public GameResult result() {
        if (!isOver()) {
            throw new IllegalStateException("The game is still in progress");
        }
        if (passedCount() >= WINNING_THRESHOLD) {
            return GameResult.KING_WINS;
        }
        if (dissolvedCount() >= WINNING_THRESHOLD) {
            return GameResult.MAGNATES_WIN;
        }
        return GameResult.DRAW;
    }

    private void requireGameInProgress() {
        if (isOver()) {
            throw new IllegalStateException("The game is over");
        }
    }

    private Bill requireSelectedBill() {
        requireGameInProgress();
        return state.selectedBill()
                .orElseThrow(() -> new InvalidChoiceException("Najpierw wybierz ustawę"));
    }
}
