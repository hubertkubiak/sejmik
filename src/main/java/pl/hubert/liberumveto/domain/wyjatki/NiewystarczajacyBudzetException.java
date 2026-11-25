package pl.hubert.liberumveto.domain.wyjatki;

import java.io.Serial;

/**
 * Próba wydania więcej dukatów, niż gracz ma w skarbcu.
 * Unchecked (RuntimeException): to błąd użycia API, któremu wywołujący może zapobiec,
 * sprawdzając budżet wcześniej.
 */
public class NiewystarczajacyBudzetException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int budzet;
    private final int kwota;

    public NiewystarczajacyBudzetException(int budzet, int kwota) {
        super("Niewystarczający budżet: masz " + budzet + " dukatów, a chcesz wydać " + kwota);
        this.budzet = budzet;
        this.kwota = kwota;
    }

    public int budzet() {
        return budzet;
    }

    public int kwota() {
        return kwota;
    }
}
