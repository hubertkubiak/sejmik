package pl.hubert.liberumveto.domain.gracz;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import pl.hubert.liberumveto.domain.model.Frakcja;
import pl.hubert.liberumveto.domain.model.Posel;
import pl.hubert.liberumveto.domain.model.TypUstawy;
import pl.hubert.liberumveto.domain.model.Ustawa;

class MagnatTest {

    private static final Ustawa USTAWA = new Ustawa(1, "Podatek", TypUstawy.PODATEK, Map.of());

    @Test
    void przekupujeNajpierwNajtanszegoIZbijaGoDoZera() {
        var lojalny = new Posel(1, "A", "A", Frakcja.KROLEWSKA, 80, 0.5);
        var chwiejny = new Posel(2, "B", "B", Frakcja.NIEZALEZNA, 20, 1.0);
        var magnat = new Magnat("Radziwiłł", 25);

        Map<Posel, Integer> rozdane = magnat.rozdzielLapowki(List.of(lojalny, chwiejny), USTAWA);

        // koszt chwiejnego: ceil((20 + 0.5) / 1.0) = 21; reszta 4 dukaty idzie do lojalnego
        assertThat(rozdane).containsExactly(Map.entry(chwiejny, 21), Map.entry(lojalny, 4));
        assertThat(chwiejny.obliczPoparcie(USTAWA)).isZero();
        assertThat(magnat.budzet()).isZero();
    }

    @Test
    void pomijaNieprzekupnychIJuzZbitychDoZera() {
        var nieprzekupny = new Posel(1, "A", "A", Frakcja.KROLEWSKA, 10, 0.0);
        var zerowy = new Posel(2, "B", "B", Frakcja.MAGNACKA, 0, 0.8);
        var magnat = new Magnat("Radziwiłł", 50);

        assertThat(magnat.rozdzielLapowki(List.of(nieprzekupny, zerowy), USTAWA)).isEmpty();
        assertThat(magnat.budzet()).isEqualTo(50);
    }

    @Test
    void kosztUwzgledniaPrzekupnosc() {
        var posel = new Posel(1, "A", "A", Frakcja.NIEZALEZNA, 30, 0.5);

        // ceil(30.5 / 0.5) = 61
        assertThat(Magnat.kosztZbiciaDoZera(posel, USTAWA)).isEqualTo(61);
    }
}
