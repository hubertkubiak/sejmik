package pl.hubert.liberumveto.console.zapis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.hubert.liberumveto.domain.gra.GeneratorPoslow;
import pl.hubert.liberumveto.domain.gra.Gra;
import pl.hubert.liberumveto.domain.gra.KatalogUstaw;
import pl.hubert.liberumveto.domain.gra.StanGry;
import pl.hubert.liberumveto.domain.wyjatki.BladZapisuGryException;

class ZapisGryTest {

    /** JUnit tworzy pusty katalog przed testem i sprząta go po teście. */
    @TempDir
    Path katalog;

    /** Gra w trakcie: dwa sejmy w historii, trzeci z wybraną ustawą i łapówkami. */
    private static Gra graWTrakcie() {
        var random = new Random(77);
        var gra = Gra.nowa(new GeneratorPoslow(List.of("Jan Zamoyski", "Michał Pac"),
                List.of("Jelita", "Gozdawa"), random), KatalogUstaw.domyslny(), random);
        for (int i = 0; i < 2; i++) {
            gra.wybierzUstawe(1);
            gra.przekup(1, 10);
            gra.glosuj();
        }
        gra.wybierzUstawe(2);
        gra.przekup(3, 25);
        return gra;
    }

    @Test
    void zapisIOdczytDajaIdentycznyStan() throws BladZapisuGryException {
        StanGry oryginal = graWTrakcie().stan();
        var zapis = new ZapisGry(katalog.resolve("save.json"));

        zapis.zapisz(oryginal);
        StanGry wczytany = zapis.wczytaj();

        // porównanie pole po polu, rekurencyjnie, łącznie z prywatnymi polami i historią
        assertThat(wczytany).usingRecursiveComparison().isEqualTo(oryginal);
        assertThat(wczytany.historia()).hasSize(2);
        assertThat(wczytany.posel(3).lapowkiKrola()).isEqualTo(25);
    }

    @Test
    void wczytanaGraDajeSieDokonczyc() throws BladZapisuGryException {
        var zapis = new ZapisGry(katalog.resolve("save.json"));
        zapis.zapisz(graWTrakcie().stan());

        var gra = new Gra(zapis.wczytaj(), KatalogUstaw.domyslny(), new Random(1));
        gra.glosuj();
        while (!gra.czyKoniec()) {
            gra.wybierzUstawe(1);
            gra.glosuj();
        }

        assertThat(gra.stan().historia()).hasSize(Gra.LICZBA_SEJMOW);
        assertThat(gra.wynik()).isNotNull();
    }

    @Test
    void zapisJestCzytelnymJsonem() throws Exception {
        Path plik = katalog.resolve("save.json");
        new ZapisGry(plik).zapisz(graWTrakcie().stan());

        String json = Files.readString(plik);

        assertThat(json).contains("\"wersja\" : 1", "\"numerSejmu\" : 3", "\"poslowie\"", "\"MAGNACKA\"");
        try (var pliki = Files.list(katalog)) {
            assertThat(pliki).containsExactly(plik); // brak osieroconych plików tymczasowych
        }
    }

    @Test
    void brakPlikuToBladZapisu() {
        var zapis = new ZapisGry(katalog.resolve("nie-ma.json"));

        assertThat(zapis.istnieje()).isFalse();
        assertThatThrownBy(zapis::wczytaj)
                .isInstanceOf(BladZapisuGryException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void uszkodzonyPlikToBladZapisu() throws IOException {
        Path plik = katalog.resolve("save.json");
        Files.writeString(plik, "{ to nie jest json");

        assertThatThrownBy(() -> new ZapisGry(plik).wczytaj())
                .isInstanceOf(BladZapisuGryException.class)
                .hasMessageContaining("uszkodzony");
    }
}
