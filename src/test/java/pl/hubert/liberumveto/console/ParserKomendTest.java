package pl.hubert.liberumveto.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pl.hubert.liberumveto.domain.wyjatki.NieprawidlowyWyborException;

class ParserKomendTest {

    private final ParserKomend parser = new ParserKomend();

    @Test
    void parsujePrzekupZArgumentami() {
        assertThat(parser.parsuj("  przekup 7   30 ")).isEqualTo(new Komenda.Przekup(7, 30));
    }

    @ParameterizedTest
    @ValueSource(strings = {"posłowie", "poslowie", "POSŁOWIE", "p"})
    void akceptujePolskieZnakiIWielkoscLiter(String tekst) {
        assertThat(parser.parsuj(tekst)).isInstanceOf(Komenda.Poslowie.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"głosuj", "glosuj", "Głosuj", "g"})
    void rozpoznajeGlosowanie(String tekst) {
        assertThat(parser.parsuj(tekst)).isEqualTo(new Komenda.Glosuj());
    }

    @Test
    void rozpoznajeStatystyki() {
        assertThat(parser.parsuj("statystyki")).isEqualTo(new Komenda.Statystyki());
        assertThat(parser.parsuj("zapisz")).isEqualTo(new Komenda.Zapisz());
    }

    @Test
    void rozpoznajePomocIWyjscie() {
        assertThat(parser.parsuj("pomoc")).isEqualTo(new Komenda.Pomoc());
        assertThat(parser.parsuj("wyjdź")).isEqualTo(new Komenda.Wyjdz());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "taniec", "przekup", "przekup 7", "przekup x 30", "przekup 7 dużo", "glosuj teraz"})
    void odrzucaBledneDane(String tekst) {
        assertThatThrownBy(() -> parser.parsuj(tekst)).isInstanceOf(NieprawidlowyWyborException.class);
    }

    @Test
    void bladLiczbyZachowujePrzyczyne() {
        assertThatThrownBy(() -> parser.parsujNumer("dwa"))
                .isInstanceOf(NieprawidlowyWyborException.class)
                .hasCauseInstanceOf(NumberFormatException.class);
        assertThat(parser.parsujNumer(" 2 ")).isEqualTo(2);
    }
}
