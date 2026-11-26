package pl.hubert.sejmik.domain.exception;

import java.io.Serial;

/**
 * An attempt to spend more ducats than the player has in the treasury.
 * Unchecked (RuntimeException): it is an API misuse the caller can prevent
 * by checking the budget first.
 */
public class InsufficientBudgetException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int budget;
    private final int amount;

    public InsufficientBudgetException(int budget, int amount) {
        // the message is shown to the player, hence Polish
        super("Niewystarczający budżet: masz " + budget + " dukatów, a chcesz wydać " + amount);
        this.budget = budget;
        this.amount = amount;
    }

    public int budget() {
        return budget;
    }

    public int amount() {
        return amount;
    }
}
