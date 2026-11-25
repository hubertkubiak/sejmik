package pl.hubert.liberumveto.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UstawaTest {

    @Test
    void zwracaWplywNaFrakcjeAlboZeroGdyBrak() {
        var ustawa = new Ustawa(1, "Podatek", TypUstawy.PODATEK,
                Map.of(Frakcja.KROLEWSKA, 20, Frakcja.MAGNACKA, -30));

        assertThat(ustawa.wplywNa(Frakcja.KROLEWSKA)).isEqualTo(20);
        assertThat(ustawa.wplywNa(Frakcja.MAGNACKA)).isEqualTo(-30);
        assertThat(ustawa.wplywNa(Frakcja.NIEZALEZNA)).isZero();
    }

    @Test
    void jestNiemutowalnaNawetGdyZmienimyOryginalnaMape() {
        var mapa = new HashMap<Frakcja, Integer>();
        mapa.put(Frakcja.KROLEWSKA, 10);
        var ustawa = new Ustawa(1, "Podatek", TypUstawy.PODATEK, mapa);

        mapa.put(Frakcja.KROLEWSKA, 99);

        assertThat(ustawa.wplywNa(Frakcja.KROLEWSKA)).isEqualTo(10);
        assertThatThrownBy(() -> ustawa.wplyw().put(Frakcja.MAGNACKA, 1))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rekordyZTymiSamymiDanymiSaRowne() {
        var a = new Ustawa(1, "Podatek", TypUstawy.PODATEK, Map.of());
        var b = new Ustawa(1, "Podatek", TypUstawy.PODATEK, Map.of());

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }

    @Test
    void odrzucaPustyTytul() {
        assertThatThrownBy(() -> new Ustawa(1, " ", TypUstawy.PODATEK, Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
