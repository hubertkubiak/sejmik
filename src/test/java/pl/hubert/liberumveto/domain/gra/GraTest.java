package pl.hubert.liberumveto.domain.gra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pl.hubert.liberumveto.domain.gracz.Krol;
import pl.hubert.liberumveto.domain.gracz.Magnat;
import pl.hubert.liberumveto.domain.model.Frakcja;
import pl.hubert.liberumveto.domain.model.Posel;
import pl.hubert.liberumveto.domain.model.TypUstawy;
import pl.hubert.liberumveto.domain.model.Ustawa;
import pl.hubert.liberumveto.domain.wyjatki.NieprawidlowyWyborException;

class GraTest {

    /** Ustawy obojętne dla wszystkich frakcji, więc poparcie zależy tylko od lojalności i łapówek. */
    private static final KatalogUstaw NEUTRALNY = new KatalogUstaw(IntStream.rangeClosed(1, 3)
            .mapToObj(i -> new Ustawa(i, "Ustawa " + i, TypUstawy.PODATEK, Map.of()))
            .toList());

    /** Gra z jednym "przewidywalnym" posłem: nieprzekupny poseł zawsze głosuje tak samo. */
    private static Gra graZPoslem(int lojalnosc, double przekupnosc) {
        var posel = new Posel(1, "Jan", "Jelita", Frakcja.NIEZALEZNA, lojalnosc, przekupnosc);
        var random = new Random(42);
        var stan = new StanGry(1, List.of(posel), new Krol("Król", 100), new Magnat("Magnaci", 160),
                List.of(), NEUTRALNY.losuj(Gra.USTAW_DO_WYBORU, random), null);
        return new Gra(stan, NEUTRALNY, random);
    }

    private static void rozegrajTure(Gra gra) {
        gra.wybierzUstawe(1);
        gra.glosuj();
    }

    @Nested
    class WarunkiKonca {

        @Test
        void krolWygrywaGdyPrzejdaCoNajmniejTrzyUstawy() {
            var gra = graZPoslem(90, 0.0);

            for (int i = 0; i < Gra.LICZBA_SEJMOW; i++) {
                rozegrajTure(gra);
            }

            assertThat(gra.czyKoniec()).isTrue();
            assertThat(gra.liczbaPrzyjetych()).isEqualTo(5);
            assertThat(gra.wynik()).isEqualTo(WynikGry.WYGRANA_KROLA);
        }

        @Test
        void magnaciWygrywajaGdyZerwanoCoNajmniejTrzySejmy() {
            var gra = graZPoslem(0, 0.0); // poparcie 0 -> veto pewne

            for (int i = 0; i < Gra.LICZBA_SEJMOW; i++) {
                rozegrajTure(gra);
            }

            assertThat(gra.liczbaZerwanych()).isEqualTo(5);
            assertThat(gra.wynik()).isEqualTo(WynikGry.WYGRANA_MAGNATOW);
        }

        @Test
        void remisGdyUstawyUpadajaBezVeta() {
            var gra = graZPoslem(30, 0.0); // zawsze PRZECIW, nigdy veto

            for (int i = 0; i < Gra.LICZBA_SEJMOW; i++) {
                rozegrajTure(gra);
            }

            assertThat(gra.liczbaPrzyjetych()).isZero();
            assertThat(gra.liczbaZerwanych()).isZero();
            assertThat(gra.wynik()).isEqualTo(WynikGry.REMIS);
        }

        @Test
        void remisPrzyDwochPrzyjetychIDwochZerwanych() {
            // poseł z poparciem 0 jest pomijany przez magnatów, a łapówka Króla decyduje o głosie
            var gra = graZPoslem(0, 1.0);

            turaZLapowka(gra, 50);  // poparcie 50 -> ZA
            turaZLapowka(gra, 60);  // ZA
            turaZLapowka(gra, 0);   // poparcie 0 -> VETO
            turaZLapowka(gra, 0);   // VETO
            turaZLapowka(gra, 30);  // PRZECIW -> ustawa upada

            assertThat(gra.liczbaPrzyjetych()).isEqualTo(2);
            assertThat(gra.liczbaZerwanych()).isEqualTo(2);
            assertThat(gra.wynik()).isEqualTo(WynikGry.REMIS);
        }

        private void turaZLapowka(Gra gra, int kwota) {
            gra.wybierzUstawe(1);
            if (kwota > 0) {
                gra.przekup(1, kwota);
            }
            gra.glosuj();
        }

        @Test
        void wynikNiedostepnyPrzedKoncem() {
            assertThatThrownBy(() -> graZPoslem(90, 0.0).wynik()).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void poKoncuNieMoznaGracDalej() {
            var gra = graZPoslem(90, 0.0);
            for (int i = 0; i < Gra.LICZBA_SEJMOW; i++) {
                rozegrajTure(gra);
            }

            assertThatThrownBy(() -> gra.wybierzUstawe(1)).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(gra::glosuj).isInstanceOf(IllegalStateException.class);
            assertThat(gra.stan().historia()).hasSize(5)
                    .extracting(WynikSejmu::numer).containsExactly(1, 2, 3, 4, 5);
        }
    }

    @Nested
    class PrzebiegTury {

        @Test
        void nowaGraMaDwudziestuPoslowITrzyUstawy() {
            var random = new Random(1);
            var gra = Gra.nowa(new GeneratorPoslow(List.of("Jan"), List.of("Jelita"), random),
                    KatalogUstaw.domyslny(), random);

            assertThat(gra.stan().poslowie()).hasSize(Gra.LICZBA_POSLOW);
            assertThat(gra.ustawyDoWyboru()).hasSize(Gra.USTAW_DO_WYBORU).doesNotHaveDuplicates();
            assertThat(gra.stan().numerSejmu()).isEqualTo(1);
            assertThat(gra.stan().krol().budzet()).isEqualTo(Gra.BUDZET_KROLA);
            assertThat(gra.wybranaUstawa()).isEmpty();
        }

        @Test
        void nieMoznaGlosowacAniPrzekupywacPrzedWyboremUstawy() {
            var gra = graZPoslem(50, 0.5);

            assertThatThrownBy(gra::glosuj).isInstanceOf(NieprawidlowyWyborException.class);
            assertThatThrownBy(() -> gra.przekup(1, 10)).isInstanceOf(NieprawidlowyWyborException.class);
        }

        @Test
        void nieMoznaWybracUstawyDwaRazyAniSpozaListy() {
            var gra = graZPoslem(50, 0.5);

            assertThatThrownBy(() -> gra.wybierzUstawe(0)).isInstanceOf(NieprawidlowyWyborException.class);
            assertThatThrownBy(() -> gra.wybierzUstawe(4)).isInstanceOf(NieprawidlowyWyborException.class);

            gra.wybierzUstawe(2);

            assertThat(gra.wybranaUstawa()).contains(gra.ustawyDoWyboru().get(1));
            assertThatThrownBy(() -> gra.wybierzUstawe(1)).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void wyborUstawyUruchamiaLapowkiMagnatow() {
            var gra = graZPoslem(40, 0.5);

            Map<Posel, Integer> lapowki = gra.wybierzUstawe(1);

            assertThat(lapowki).isNotEmpty();
            assertThat(gra.stan().poslowie().getFirst().lapowkiMagnatow()).isPositive();
        }

        @Test
        void przekupstwoZwracaZmianePoparcia() {
            var gra = graZPoslem(0, 1.0);
            gra.wybierzUstawe(1);

            ZmianaPoparcia zmiana = gra.przekup(1, 30);

            assertThat(zmiana.przed()).isZero();
            assertThat(zmiana.po()).isEqualTo(30);
            assertThat(gra.stan().krol().budzet()).isEqualTo(70);
        }

        @Test
        void przekupstwoNieistniejacegoPoslaRzucaWyjatek() {
            var gra = graZPoslem(50, 0.5);
            gra.wybierzUstawe(1);

            assertThatThrownBy(() -> gra.przekup(99, 10))
                    .isInstanceOf(NieprawidlowyWyborException.class)
                    .hasMessageContaining("99");
        }

        @Test
        void poGlosowaniuNastepnySejmZNowymiBudzetamiIBezLapowek() {
            var gra = graZPoslem(0, 1.0);
            gra.wybierzUstawe(1);
            gra.przekup(1, 60); // ZA

            WynikSejmu wynik = gra.glosuj();

            assertThat(wynik.wynik().przyjeta()).isTrue();
            assertThat(gra.stan().numerSejmu()).isEqualTo(2);
            assertThat(gra.wybranaUstawa()).isEmpty();
            assertThat(gra.stan().poslowie().getFirst().lapowkiKrola()).isZero();
            assertThat(gra.stan().krol().budzet())
                    .isEqualTo(Gra.BUDZET_KROLA + Gra.BONUS_KROLA_ZA_USTAWE);
            assertThat(gra.stan().magnat().budzet()).isEqualTo(Gra.BUDZET_MAGNATA);
        }

        @Test
        void zerwanySejmTrafiaDoHistoriiZeZrywajacym() {
            var gra = graZPoslem(0, 0.0);
            gra.wybierzUstawe(1);

            WynikSejmu wynik = gra.glosuj();

            assertThat(wynik.wynik().zerwany()).isTrue();
            assertThat(wynik.wynik().zrywajacy()).map(Posel::id).contains(1);
            assertThat(gra.stan().magnat().budzet())
                    .isEqualTo(Gra.BUDZET_MAGNATA + Gra.BONUS_MAGNATA_ZA_ZERWANIE);
        }
    }
}
