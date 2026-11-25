package pl.hubert.liberumveto.domain.sejm;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import pl.hubert.liberumveto.domain.model.Glos;
import pl.hubert.liberumveto.domain.model.Posel;
import pl.hubert.liberumveto.domain.model.Ustawa;
import pl.hubert.liberumveto.domain.wyjatki.SejmZerwanyException;

/**
 * Przeprowadza jedno głosowanie nad ustawą.
 */
public class Sejm {

    private final List<Posel> poslowie;
    private final Random random;

    public Sejm(List<Posel> poslowie, Random random) {
        this.poslowie = List.copyOf(poslowie);
        this.random = Objects.requireNonNull(random, "random");
        if (this.poslowie.isEmpty()) {
            throw new IllegalArgumentException("Sejm bez posłów nie może obradować");
        }
    }

    /**
     * Każdy poseł oddaje głos (kolejność listy ma znaczenie dla losowania).
     * Ustawa przechodzi, gdy głosów ZA jest więcej niż PRZECIW. Jedno veto zrywa sejm.
     *
     * @throws SejmZerwanyException gdy choć jeden poseł zgłosił veto
     */
    public WynikGlosowania obraduj(Ustawa ustawa) throws SejmZerwanyException {
        Objects.requireNonNull(ustawa, "ustawa");

        // LinkedHashMap zachowuje kolejność, więc "pierwszy zrywający" jest jednoznaczny
        Map<Posel, Glos> glosy = poslowie.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        posel -> posel.zaglosuj(ustawa, random),
                        (a, b) -> a,
                        LinkedHashMap::new));

        Map<Glos, Long> liczbaGlosow = glosy.values().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Optional<Posel> zrywajacy = glosy.entrySet().stream()
                .filter(wpis -> wpis.getValue() == Glos.VETO)
                .map(Map.Entry::getKey)
                .findFirst();

        if (zrywajacy.isPresent()) {
            var wynik = new WynikGlosowania(liczbaGlosow, false, zrywajacy);
            throw new SejmZerwanyException(zrywajacy.get(), wynik);
        }

        long za = liczbaGlosow.getOrDefault(Glos.ZA, 0L);
        long przeciw = liczbaGlosow.getOrDefault(Glos.PRZECIW, 0L);
        return new WynikGlosowania(liczbaGlosow, za > przeciw, Optional.empty());
    }
}
