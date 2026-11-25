package pl.hubert.liberumveto.domain.sejm;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import pl.hubert.liberumveto.domain.model.Glos;
import pl.hubert.liberumveto.domain.model.Posel;

/**
 * Wynik jednego głosowania.
 *
 * @param liczbaGlosow ile było głosów każdego rodzaju (zawsze zawiera wszystkie trzy klucze)
 * @param przyjeta     czy ustawa przeszła
 * @param zrywajacy    poseł, który zerwał sejm, jeśli ktoś to zrobił
 */
public record WynikGlosowania(Map<Glos, Long> liczbaGlosow, boolean przyjeta, Optional<Posel> zrywajacy) {

    public WynikGlosowania {
        Objects.requireNonNull(liczbaGlosow, "liczbaGlosow");
        Objects.requireNonNull(zrywajacy, "zrywajacy");
        // uzupełniamy brakujące rodzaje głosów zerami, żeby nie trzeba było sprawdzać nulli
        var pelna = new EnumMap<Glos, Long>(Glos.class);
        for (Glos glos : Glos.values()) {
            pelna.put(glos, liczbaGlosow.getOrDefault(glos, 0L));
        }
        liczbaGlosow = Map.copyOf(pelna);
        if (przyjeta && zrywajacy.isPresent()) {
            throw new IllegalArgumentException("Zerwany sejm nie może przyjąć ustawy");
        }
    }

    public long glosy(Glos glos) {
        return liczbaGlosow.get(glos);
    }

    public boolean zerwany() {
        return zrywajacy.isPresent();
    }
}
