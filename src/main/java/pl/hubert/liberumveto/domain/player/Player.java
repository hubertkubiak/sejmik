package pl.hubert.liberumveto.domain.player;

import java.util.Objects;
import pl.hubert.liberumveto.domain.exception.InsufficientBudgetException;
import pl.hubert.liberumveto.domain.model.Deputy;

/**
 * What the King and the Magnate have in common: a name, a treasury and bribing deputies.
 * An abstract class: you cannot create "just a player", only a concrete subclass.
 */
public abstract class Player {

    private final String name;
    private int budget;

    protected Player(String name, int budget) {
        this.name = Objects.requireNonNull(name, "name");
        this.budget = requireNonNegative(budget);
    }

    /**
     * The "template method" pattern: the shared algorithm (validation and taking the ducats) lives here,
     * while the step that differs (whose bribe it is) is supplied by subclasses.
     * The method is final so a subclass cannot bypass the budget check.
     */
    public final void bribe(Deputy deputy, int amount) {
        Objects.requireNonNull(deputy, "deputy");
        if (amount <= 0) {
            // the player can type a non-positive amount, so the message is in Polish
            throw new IllegalArgumentException("Kwota łapówki musi być dodatnia: " + amount);
        }
        if (amount > budget) {
            throw new InsufficientBudgetException(budget, amount);
        }
        budget -= amount;
        deliverBribe(deputy, amount);
    }

    protected abstract void deliverBribe(Deputy deputy, int amount);

    /** Sets the treasury at the start of a new Sejm. */
    public void setBudget(int newBudget) {
        this.budget = requireNonNegative(newBudget);
    }

    private static int requireNonNegative(int budget) {
        if (budget < 0) {
            throw new IllegalArgumentException("Budget must not be negative: " + budget);
        }
        return budget;
    }

    public String name() {
        return name;
    }

    public int budget() {
        return budget;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + ", budget=" + budget + "]";
    }
}
