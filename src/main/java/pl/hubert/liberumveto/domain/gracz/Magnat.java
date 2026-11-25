package pl.hubert.liberumveto.domain.gracz;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import pl.hubert.liberumveto.domain.model.Posel;
import pl.hubert.liberumveto.domain.model.Ustawa;

/**
 * Przeciwnik sterowany przez komputer. Jego łapówki obniżają poparcie, a celem jest choć jedno veto.
 */
public class Magnat extends Gracz {

    public Magnat(String nazwa, int budzet) {
        super(nazwa, budzet);
    }

    @Override
    protected void przekazLapowke(Posel posel, int kwota) {
        posel.przyjmijLapowkeOdMagnatow(kwota);
    }

    /**
     * Strategia AI: wystarczy jedno veto, więc magnat wybiera posłów, których najtaniej zepchnąć
     * do zera poparcia (wtedy veto jest pewne), i przekupuje ich po kolei, póki starcza dukatów.
     * Resztę budżetu wydaje na kolejnego kandydata, żeby choć zwiększyć szansę na veto.
     *
     * @return kto i ile dostał (w kolejności przekupywania)
     */
    public Map<Posel, Integer> rozdzielLapowki(List<Posel> poslowie, Ustawa ustawa) {
        List<Posel> kandydaci = poslowie.stream()
                .filter(posel -> posel.przekupnosc() > 0)
                .filter(posel -> posel.obliczPoparcie(ustawa) > 0)
                .sorted(Comparator.comparingInt((Posel posel) -> kosztZbiciaDoZera(posel, ustawa))
                        .thenComparingInt(Posel::id))
                .toList();

        Map<Posel, Integer> rozdane = new LinkedHashMap<>();
        for (Posel posel : kandydaci) {
            if (budzet() == 0) {
                break;
            }
            int kwota = Math.min(kosztZbiciaDoZera(posel, ustawa), budzet());
            przekup(posel, kwota);
            rozdane.put(posel, kwota);
        }
        // Map.copyOf zgubiłby kolejność, więc opakowujemy LinkedHashMap w widok tylko do odczytu
        return Collections.unmodifiableMap(rozdane);
    }

    /**
     * Ile dukatów trzeba dać posłowi, żeby jego poparcie spadło do 0.
     * +0.5 kompensuje zaokrąglanie w {@link Posel#obliczPoparcie(Ustawa)}.
     */
    static int kosztZbiciaDoZera(Posel posel, Ustawa ustawa) {
        return (int) Math.ceil((posel.obliczPoparcie(ustawa) + 0.5) / posel.przekupnosc());
    }
}
