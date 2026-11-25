package pl.hubert.liberumveto.domain.gra;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import pl.hubert.liberumveto.domain.model.Frakcja;
import pl.hubert.liberumveto.domain.model.Posel;
import pl.hubert.liberumveto.domain.model.Ustawa;

/**
 * Statystyki sejmu (F7), liczone w całości na Stream API.
 * Obiekt niemutowalny: dostaje migawkę listy posłów i tylko z niej czyta.
 */
public class Statystyki {

    private final List<Posel> poslowie;

    public Statystyki(List<Posel> poslowie) {
        this.poslowie = List.copyOf(poslowie);
    }

    /** Średnia lojalność wobec Króla w każdej frakcji (frakcje bez posłów pomijamy). */
    public Map<Frakcja, Double> sredniaLojalnoscPerFrakcja() {
        return poslowie.stream()
                .collect(Collectors.groupingBy(
                        Posel::frakcja,
                        () -> new EnumMap<>(Frakcja.class),
                        Collectors.averagingInt(Posel::lojalnoscWobecKrola)));
    }

    /** {@code ile} najbardziej przekupnych posłów, od najbardziej przekupnego. */
    public List<Posel> najbardziejPrzekupni(int ile) {
        return poslowie.stream()
                .sorted(Comparator.comparingDouble(Posel::przekupnosc).reversed()
                        .thenComparingInt(Posel::id))
                .limit(ile)
                .toList();
    }

    /** Posłowie, którzy przy danej ustawie mogą zgłosić veto, od najbardziej zagrożonych. */
    public List<Posel> zagrozeniVetem(Ustawa ustawa) {
        return poslowie.stream()
                .filter(posel -> posel.obliczPoparcie(ustawa) < Posel.PROG_VETA)
                .sorted(Comparator.comparingInt((Posel posel) -> posel.obliczPoparcie(ustawa))
                        .thenComparingInt(Posel::id))
                .toList();
    }

    /** Ilu posłów z każdej frakcji zasiada w sejmie. */
    public Map<Frakcja, Long> liczebnoscFrakcji() {
        return poslowie.stream()
                .collect(Collectors.groupingBy(Posel::frakcja, () -> new EnumMap<>(Frakcja.class),
                        Collectors.counting()));
    }

    /** Średnie poparcie dla ustawy w całym sejmie. */
    public double sredniePoparcie(Ustawa ustawa) {
        return poslowie.stream()
                .mapToInt(posel -> posel.obliczPoparcie(ustawa))
                .average()
                .orElse(0.0);
    }
}
