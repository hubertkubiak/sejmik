package pl.hubert.liberumveto.domain.model;

import java.io.Serial;
import java.util.Random;

/**
 * Atrapa (test double) generatora losowego: nextDouble() zawsze zwraca tę samą wartość.
 * Dzięki temu "rzut kostką" przy vecie jest w testach przewidywalny.
 */
public class StalyRandom extends Random {

    @Serial
    private static final long serialVersionUID = 1L;

    private final double wartosc;

    public StalyRandom(double wartosc) {
        this.wartosc = wartosc;
    }

    @Override
    public double nextDouble() {
        return wartosc;
    }
}
