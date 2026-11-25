package pl.hubert.liberumveto.domain.gra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import pl.hubert.liberumveto.domain.model.Frakcja;
import pl.hubert.liberumveto.domain.model.Posel;
import pl.hubert.liberumveto.domain.model.TypUstawy;
import pl.hubert.liberumveto.domain.model.Ustawa;

class StatystykiTest {

    private static final Ustawa PODATEK = new Ustawa(1, "Podatek", TypUstawy.PODATEK,
            Map.of(Frakcja.MAGNACKA, -30));

    private final List<Posel> poslowie = List.of(
            new Posel(1, "A", "A", Frakcja.KROLEWSKA, 80, 0.2),
            new Posel(2, "B", "B", Frakcja.KROLEWSKA, 60, 0.9),
            new Posel(3, "C", "C", Frakcja.MAGNACKA, 40, 0.5),   // 40 - 30 = 10 -> zagrożony
            new Posel(4, "D", "D", Frakcja.MAGNACKA, 20, 0.9),   // 20 - 30 -> 0 -> zagrożony
            new Posel(5, "E", "E", Frakcja.NIEZALEZNA, 50, 0.7));

    private final Statystyki statystyki = new Statystyki(poslowie);

    @Test
    void liczySredniaLojalnoscWKazdejFrakcji() {
        assertThat(statystyki.sredniaLojalnoscPerFrakcja())
                .containsExactly(
                        Map.entry(Frakcja.KROLEWSKA, 70.0),
                        Map.entry(Frakcja.MAGNACKA, 30.0),
                        Map.entry(Frakcja.NIEZALEZNA, 50.0));
    }

    @Test
    void zwracaTopTrzechNajbardziejPrzekupnych() {
        // remis 0.9 rozstrzyga niższe id
        assertThat(statystyki.najbardziejPrzekupni(3))
                .extracting(Posel::id)
                .containsExactly(2, 4, 5);
    }

    @Test
    void wskazujeZagrozonychVetemOdNajgorszego() {
        assertThat(statystyki.zagrozeniVetem(PODATEK))
                .extracting(Posel::id)
                .containsExactly(4, 3);
    }

    @Test
    void liczyLiczebnoscIPoparcie() {
        assertThat(statystyki.liczebnoscFrakcji())
                .containsEntry(Frakcja.KROLEWSKA, 2L)
                .containsEntry(Frakcja.MAGNACKA, 2L)
                .containsEntry(Frakcja.NIEZALEZNA, 1L);
        // (80 + 60 + 10 + 0 + 50) / 5 = 40
        assertThat(statystyki.sredniePoparcie(PODATEK)).isCloseTo(40.0, within(0.001));
    }

    @Test
    void pustySejmDajePusteStatystyki() {
        var puste = new Statystyki(List.of());

        assertThat(puste.sredniaLojalnoscPerFrakcja()).isEmpty();
        assertThat(puste.najbardziejPrzekupni(3)).isEmpty();
        assertThat(puste.sredniePoparcie(PODATEK)).isZero();
    }
}
