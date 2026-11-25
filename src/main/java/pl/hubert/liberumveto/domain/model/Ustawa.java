package pl.hubert.liberumveto.domain.model;

import java.util.Map;
import java.util.Objects;

/**
 * Projekt ustawy poddawany pod głosowanie.
 *
 * @param id    unikalny identyfikator
 * @param tytul nazwa wyświetlana graczowi
 * @param typ   kategoria ustawy
 * @param wplyw jak bardzo ustawa zmienia poparcie posłów danej frakcji (np. KROLEWSKA +20, MAGNACKA -30)
 */
public record Ustawa(int id, String tytul, TypUstawy typ, Map<Frakcja, Integer> wplyw) {

    /**
     * Kompaktowy konstruktor: walidacja i kopia obronna. Pola przypisuje kompilator.
     */
    public Ustawa {
        Objects.requireNonNull(tytul, "tytul");
        Objects.requireNonNull(typ, "typ");
        if (tytul.isBlank()) {
            throw new IllegalArgumentException("Tytuł ustawy nie może być pusty");
        }
        // Map.copyOf tworzy niemutowalną kopię, więc nikt z zewnątrz nie zmieni wpływu ustawy
        wplyw = Map.copyOf(Objects.requireNonNull(wplyw, "wplyw"));
    }

    /** Wpływ ustawy na daną frakcję; 0, jeśli frakcja jest jej obojętna. */
    public int wplywNa(Frakcja frakcja) {
        return wplyw.getOrDefault(frakcja, 0);
    }
}
