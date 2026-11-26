package pl.hubert.liberumveto.domain.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import pl.hubert.liberumveto.domain.exception.InvalidChoiceException;
import pl.hubert.liberumveto.domain.model.Bill;
import pl.hubert.liberumveto.domain.model.Deputy;
import pl.hubert.liberumveto.domain.player.King;
import pl.hubert.liberumveto.domain.player.Magnate;

/**
 * The whole game state in one place, without the rules (those live in {@link Game}).
 * Everything here has to end up in the save file, so the game loads without losing state.
 */
public class GameState {

    private int sejmNumber;
    private final List<Deputy> deputies;
    private final King king;
    private final Magnate magnate;
    private final List<SejmResult> history;
    private List<Bill> availableBills;
    private Bill selectedBill;

    public GameState(int sejmNumber, List<Deputy> deputies, King king, Magnate magnate,
                     List<SejmResult> history, List<Bill> availableBills, Bill selectedBill) {
        if (sejmNumber < 1) {
            throw new IllegalArgumentException("Sejm numbering starts at 1: " + sejmNumber);
        }
        this.sejmNumber = sejmNumber;
        // deputies are mutable (bribes), but the list of deputies itself never changes
        this.deputies = List.copyOf(deputies);
        this.king = Objects.requireNonNull(king, "king");
        this.magnate = Objects.requireNonNull(magnate, "magnate");
        // history grows during the game, so we keep our own mutable copy
        this.history = new ArrayList<>(history);
        this.availableBills = List.copyOf(availableBills);
        this.selectedBill = selectedBill;
    }

    public int sejmNumber() {
        return sejmNumber;
    }

    public List<Deputy> deputies() {
        return deputies;
    }

    public Deputy deputy(int id) {
        return deputies.stream()
                .filter(deputy -> deputy.id() == id)
                .findFirst()
                .orElseThrow(() -> new InvalidChoiceException("Nie ma posła o id " + id));
    }

    public King king() {
        return king;
    }

    public Magnate magnate() {
        return magnate;
    }

    /** A read-only copy: nobody outside can append a fake result. */
    public List<SejmResult> history() {
        return List.copyOf(history);
    }

    public List<Bill> availableBills() {
        return availableBills;
    }

    public Optional<Bill> selectedBill() {
        return Optional.ofNullable(selectedBill);
    }

    // Mutators are package-private: only Game (same package) may change the state.

    void addToHistory(SejmResult result) {
        history.add(Objects.requireNonNull(result));
    }

    void setAvailableBills(List<Bill> bills) {
        this.availableBills = List.copyOf(bills);
    }

    void setSelectedBill(Bill bill) {
        this.selectedBill = bill;
    }

    void nextSejm() {
        sejmNumber++;
    }
}
