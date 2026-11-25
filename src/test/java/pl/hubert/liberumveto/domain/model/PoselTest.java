package pl.hubert.liberumveto.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PoselTest {

    private static final Ustawa NEUTRALNA = new Ustawa(1, "Neutralna", TypUstawy.PODATEK, Map.of());
    private static final Random ZAWSZE_VETO = new StalyRandom(0.0);
    private static final Random NIGDY_VETO = new StalyRandom(0.999);

    private static Posel poselZLojalnoscia(int lojalnosc) {
        return new Posel(1, "Jan Zamoyski", "Jelita", Frakcja.NIEZALEZNA, lojalnosc, 0.5);
    }

    @Nested
    @DisplayName("Decyzja na granicach progów")
    class Progi {

        @ParameterizedTest(name = "poparcie {0} -> ZA")
        @ValueSource(ints = {50, 51, 100})
        void glosujeZaOdPiecdziesieciu(int poparcie) {
            assertThat(poselZLojalnoscia(poparcie).zaglosuj(NEUTRALNA, ZAWSZE_VETO)).isEqualTo(Glos.ZA);
        }

        @ParameterizedTest(name = "poparcie {0} -> PRZECIW")
        @ValueSource(ints = {15, 16, 49})
        void glosujePrzeciwMiedzyProgami(int poparcie) {
            // nawet "pechowy" random nie wywoła veta powyżej progu 15
            assertThat(poselZLojalnoscia(poparcie).zaglosuj(NEUTRALNA, ZAWSZE_VETO)).isEqualTo(Glos.PRZECIW);
        }

        @ParameterizedTest(name = "poparcie {0} -> VETO przy pechowym losowaniu")
        @ValueSource(ints = {0, 7, 14})
        void zglaszaVetoPonizejPietnastuGdyLosPozwoli(int poparcie) {
            assertThat(poselZLojalnoscia(poparcie).zaglosuj(NEUTRALNA, ZAWSZE_VETO)).isEqualTo(Glos.VETO);
        }

        @ParameterizedTest(name = "poparcie {0} -> PRZECIW przy szczęśliwym losowaniu")
        @ValueSource(ints = {1, 7, 14})
        void glosujePrzeciwPonizejPietnastuGdyLosNieSprzyja(int poparcie) {
            assertThat(poselZLojalnoscia(poparcie).zaglosuj(NEUTRALNA, NIGDY_VETO)).isEqualTo(Glos.PRZECIW);
        }

        @Test
        void przyZerowymPoparciuVetoJestPewne() {
            // szansa = (15 - 0) / 15 = 1.0, a nextDouble() < 1.0 zawsze
            assertThat(poselZLojalnoscia(0).zaglosuj(NEUTRALNA, NIGDY_VETO)).isEqualTo(Glos.VETO);
        }

        @Test
        void szansaNaVetoRosnieLiniowo() {
            // poparcie 10 -> szansa (15-10)/15 = 0.333...
            var posel = poselZLojalnoscia(10);
            assertThat(posel.zaglosuj(NEUTRALNA, new StalyRandom(0.33))).isEqualTo(Glos.VETO);
            assertThat(posel.zaglosuj(NEUTRALNA, new StalyRandom(0.34))).isEqualTo(Glos.PRZECIW);
        }
    }

    @Nested
    @DisplayName("Algorytm poparcia")
    class Poparcie {

        @ParameterizedTest(name = "lojalność {0}, wpływ {1}, król {2}, magnaci {3}, przekupność {4} -> {5}")
        @CsvSource({
                // lojalnosc, wplyw, lapowkiKrola, lapowkiMagnatow, przekupnosc, oczekiwane
                "40,   0,   0,   0, 0.5,  40",
                "40,  20,   0,   0, 0.5,  60",
                "40, -30,   0,   0, 0.5,  10",
                "40,   0,  30,   0, 0.5,  55",
                "40,   0,   0,  30, 0.5,  25",
                "40,   0,  30,  30, 1.0,  40",
                "90,  30,   0,   0, 0.5, 100",   // obcięcie z góry
                "10, -30,   0,  50, 1.0,   0",   // obcięcie z dołu
                "20,   0,  10,   0, 0.0,  20",   // nieprzekupny poseł ignoruje łapówki
        })
        void liczyPoparcieWedlugWzoru(int lojalnosc, int wplyw, int krol, int magnaci,
                                      double przekupnosc, int oczekiwane) {
            var ustawa = new Ustawa(2, "Test", TypUstawy.PODATEK, Map.of(Frakcja.MAGNACKA, wplyw));
            var posel = new Posel(1, "Jan", "Ogończyk", Frakcja.MAGNACKA, lojalnosc, przekupnosc, krol, magnaci);

            assertThat(posel.obliczPoparcie(ustawa)).isEqualTo(oczekiwane);
        }

        @Test
        void wplywUstawyZalezyOdFrakcjiPosla() {
            var ustawa = new Ustawa(3, "Podatek", TypUstawy.PODATEK,
                    Map.of(Frakcja.KROLEWSKA, 20, Frakcja.MAGNACKA, -30));
            var krolewski = new Posel(1, "A", "B", Frakcja.KROLEWSKA, 40, 0.5);
            var magnacki = new Posel(2, "C", "D", Frakcja.MAGNACKA, 40, 0.5);

            assertThat(krolewski.obliczPoparcie(ustawa)).isEqualTo(60);
            assertThat(magnacki.obliczPoparcie(ustawa)).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Łapówki")
    class Lapowki {

        @Test
        void lapowkiSumujaSieICzyszcza() {
            var posel = poselZLojalnoscia(30);
            posel.przyjmijLapowkeOdKrola(10);
            posel.przyjmijLapowkeOdKrola(20);
            posel.przyjmijLapowkeOdMagnatow(5);

            assertThat(posel.lapowkiKrola()).isEqualTo(30);
            assertThat(posel.lapowkiMagnatow()).isEqualTo(5);

            posel.wyczyscLapowki();

            assertThat(posel.lapowkiKrola()).isZero();
            assertThat(posel.lapowkiMagnatow()).isZero();
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -5})
        void odrzucaNiedodatniaKwote(int kwota) {
            assertThatThrownBy(() -> poselZLojalnoscia(30).przyjmijLapowkeOdKrola(kwota))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void odrzucaNieprawidloweAtrybuty() {
        assertThatThrownBy(() -> poselZLojalnoscia(101)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Posel(1, "A", "B", Frakcja.KROLEWSKA, 50, 1.5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rownoscOpartaNaId() {
        var a = new Posel(7, "Jan", "Jelita", Frakcja.KROLEWSKA, 50, 0.5);
        var b = new Posel(7, "Jan", "Jelita", Frakcja.KROLEWSKA, 50, 0.5);
        b.przyjmijLapowkeOdKrola(10);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a.pelneImie()).isEqualTo("Jan h. Jelita");
    }
}
