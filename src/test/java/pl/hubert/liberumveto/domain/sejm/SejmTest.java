package pl.hubert.liberumveto.domain.sejm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;
import pl.hubert.liberumveto.domain.model.Frakcja;
import pl.hubert.liberumveto.domain.model.Glos;
import pl.hubert.liberumveto.domain.model.Posel;
import pl.hubert.liberumveto.domain.model.StalyRandom;
import pl.hubert.liberumveto.domain.model.TypUstawy;
import pl.hubert.liberumveto.domain.model.Ustawa;
import pl.hubert.liberumveto.domain.wyjatki.SejmZerwanyException;

class SejmTest {

    private static final Ustawa USTAWA = new Ustawa(1, "Podatek", TypUstawy.PODATEK, Map.of());
    private static final Random PECHOWY = new StalyRandom(0.0);

    private static Posel posel(int id, int poparcie) {
        return new Posel(id, "Posel" + id, "Herb", Frakcja.NIEZALEZNA, poparcie, 0.5);
    }

    @Test
    void ustawaPrzechodziPrzyWiekszosci() throws SejmZerwanyException {
        var sejm = new Sejm(List.of(posel(1, 80), posel(2, 60), posel(3, 30)), PECHOWY);

        WynikGlosowania wynik = sejm.obraduj(USTAWA);

        assertThat(wynik.przyjeta()).isTrue();
        assertThat(wynik.zerwany()).isFalse();
        assertThat(wynik.glosy(Glos.ZA)).isEqualTo(2);
        assertThat(wynik.glosy(Glos.PRZECIW)).isEqualTo(1);
        assertThat(wynik.glosy(Glos.VETO)).isZero();
    }

    @Test
    void ustawaUpadaPrzyRemisie() throws SejmZerwanyException {
        var sejm = new Sejm(List.of(posel(1, 80), posel(2, 30)), PECHOWY);

        assertThat(sejm.obraduj(USTAWA).przyjeta()).isFalse();
    }

    @Test
    void jednoVetoZrywaSejm() {
        var sejm = new Sejm(List.of(posel(1, 90), posel(2, 90), posel(3, 90), posel(4, 5)), PECHOWY);

        SejmZerwanyException wyjatek = assertThrows(SejmZerwanyException.class, () -> sejm.obraduj(USTAWA));

        assertThat(wyjatek.zrywajacy().id()).isEqualTo(4);
        assertThat(wyjatek.wynik().przyjeta()).isFalse();
        assertThat(wyjatek.wynik().glosy(Glos.ZA)).isEqualTo(3);
        assertThat(wyjatek.wynik().glosy(Glos.VETO)).isEqualTo(1);
        assertThat(wyjatek.getMessage()).contains("Posel4 h. Herb");
    }

    @Test
    void zrywajacymJestPierwszyKtoZglosilVeto() {
        var sejm = new Sejm(List.of(posel(1, 90), posel(2, 3), posel(3, 1)), PECHOWY);

        assertThatThrownBy(() -> sejm.obraduj(USTAWA))
                .isInstanceOf(SejmZerwanyException.class)
                .extracting(e -> ((SejmZerwanyException) e).zrywajacy().id())
                .isEqualTo(2);
    }

    @Test
    void wynikZawszeMaWszystkieRodzajeGlosow() {
        var wynik = new WynikGlosowania(Map.of(Glos.ZA, 3L), true, Optional.empty());

        assertThat(wynik.liczbaGlosow()).containsOnlyKeys(Glos.values());
        assertThat(wynik.glosy(Glos.PRZECIW)).isZero();
    }

    @Test
    void sejmBezPoslowNieMozeObradowac() {
        assertThatThrownBy(() -> new Sejm(List.of(), PECHOWY)).isInstanceOf(IllegalArgumentException.class);
    }
}
