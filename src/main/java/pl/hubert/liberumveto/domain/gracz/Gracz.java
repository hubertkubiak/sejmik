package pl.hubert.liberumveto.domain.gracz;

import java.util.Objects;
import pl.hubert.liberumveto.domain.model.Posel;
import pl.hubert.liberumveto.domain.wyjatki.NiewystarczajacyBudzetException;

/**
 * Wspólna część Króla i Magnata: nazwa, skarbiec i przekupywanie posłów.
 * Klasa abstrakcyjna: nie da się stworzyć "po prostu gracza", tylko konkretną podklasę.
 */
public abstract class Gracz {

    private final String nazwa;
    private int budzet;

    protected Gracz(String nazwa, int budzet) {
        this.nazwa = Objects.requireNonNull(nazwa, "nazwa");
        this.budzet = wymagajNieujemnego(budzet);
    }

    /**
     * Wzorzec "metoda szablonowa": wspólny algorytm (walidacja i pobranie dukatów) jest tutaj,
     * a krok, który się różni (komu przypisać łapówkę), dostarczają podklasy.
     * Metoda jest final, żeby podklasa nie mogła obejść sprawdzania budżetu.
     */
    public final void przekup(Posel posel, int kwota) {
        Objects.requireNonNull(posel, "posel");
        if (kwota <= 0) {
            throw new IllegalArgumentException("Kwota łapówki musi być dodatnia: " + kwota);
        }
        if (kwota > budzet) {
            throw new NiewystarczajacyBudzetException(budzet, kwota);
        }
        budzet -= kwota;
        przekazLapowke(posel, kwota);
    }

    protected abstract void przekazLapowke(Posel posel, int kwota);

    /** Ustawia skarbiec na początku nowego sejmu. */
    public void ustawBudzet(int nowyBudzet) {
        this.budzet = wymagajNieujemnego(nowyBudzet);
    }

    private static int wymagajNieujemnego(int budzet) {
        if (budzet < 0) {
            throw new IllegalArgumentException("Budżet nie może być ujemny: " + budzet);
        }
        return budzet;
    }

    public String nazwa() {
        return nazwa;
    }

    public int budzet() {
        return budzet;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + nazwa + ", budżet=" + budzet + "]";
    }
}
