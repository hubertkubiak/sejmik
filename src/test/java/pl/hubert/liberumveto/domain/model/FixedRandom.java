package pl.hubert.liberumveto.domain.model;

import java.io.Serial;
import java.util.Random;

/**
 * A test double for the random generator: nextDouble() always returns the same value.
 * This makes the veto "dice roll" predictable in tests.
 */
public class FixedRandom extends Random {

    @Serial
    private static final long serialVersionUID = 1L;

    private final double value;

    public FixedRandom(double value) {
        this.value = value;
    }

    @Override
    public double nextDouble() {
        return value;
    }
}
