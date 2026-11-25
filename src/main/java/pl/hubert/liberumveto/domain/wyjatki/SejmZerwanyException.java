package pl.hubert.liberumveto.domain.wyjatki;

import java.io.Serial;
import java.util.Objects;
import pl.hubert.liberumveto.domain.model.Posel;
import pl.hubert.liberumveto.domain.sejm.WynikGlosowania;

/**
 * Poseł krzyknął "Nie pozwalam!" i sejm został zerwany.
 * Checked (extends Exception): to normalne, przewidziane zdarzenie w grze, a kompilator wymusza
 * na wywołującym świadomą obsługę tej sytuacji.
 */
public class SejmZerwanyException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    // transient: pola nie biorą udziału w serializacji Javy (Posel nie jest Serializable)
    private final transient Posel zrywajacy;
    private final transient WynikGlosowania wynik;

    public SejmZerwanyException(Posel zrywajacy, WynikGlosowania wynik) {
        super("Sejm zerwany przez: " + zrywajacy.pelneImie());
        this.zrywajacy = Objects.requireNonNull(zrywajacy, "zrywajacy");
        this.wynik = Objects.requireNonNull(wynik, "wynik");
    }

    public Posel zrywajacy() {
        return zrywajacy;
    }

    /** Pełny wynik głosowania, żeby mimo zerwania pokazać, jak głosowano. */
    public WynikGlosowania wynik() {
        return wynik;
    }
}
