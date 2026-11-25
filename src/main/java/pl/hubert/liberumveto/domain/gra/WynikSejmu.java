package pl.hubert.liberumveto.domain.gra;

import java.util.Objects;
import pl.hubert.liberumveto.domain.model.Ustawa;
import pl.hubert.liberumveto.domain.sejm.WynikGlosowania;

/**
 * Wpis w historii gry: który to był sejm, nad czym głosowano i jak się skończyło.
 */
public record WynikSejmu(int numer, Ustawa ustawa, WynikGlosowania wynik) {

    public WynikSejmu {
        Objects.requireNonNull(ustawa, "ustawa");
        Objects.requireNonNull(wynik, "wynik");
    }
}
