package pl.hubert.liberumveto.domain.gracz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.hubert.liberumveto.domain.model.Frakcja;
import pl.hubert.liberumveto.domain.model.Posel;
import pl.hubert.liberumveto.domain.wyjatki.NiewystarczajacyBudzetException;

class KrolTest {

    private Krol krol;
    private Posel posel;

    @BeforeEach
    void setUp() {
        // świeże obiekty przed każdym testem, więc testy nie wpływają na siebie nawzajem
        krol = new Krol("Zygmunt", 100);
        posel = new Posel(7, "Jan Zamoyski", "Jelita", Frakcja.NIEZALEZNA, 30, 0.5);
    }

    @Test
    void przekupstwoZmniejszaBudzetIPrzypisujeLapowkeKrolewska() {
        krol.przekup(posel, 30);

        assertThat(krol.budzet()).isEqualTo(70);
        assertThat(posel.lapowkiKrola()).isEqualTo(30);
        assertThat(posel.lapowkiMagnatow()).isZero();
    }

    @Test
    void moznaWydacCalyBudzet() {
        krol.przekup(posel, 100);

        assertThat(krol.budzet()).isZero();
    }

    @Test
    void przekroczenieBudzetuRzucaWyjatekINicNieZmienia() {
        assertThatThrownBy(() -> krol.przekup(posel, 101))
                .isInstanceOf(NiewystarczajacyBudzetException.class)
                .hasMessageContaining("100")
                .satisfies(e -> assertThat(((NiewystarczajacyBudzetException) e).kwota()).isEqualTo(101));

        assertThat(krol.budzet()).isEqualTo(100);
        assertThat(posel.lapowkiKrola()).isZero();
    }

    @Test
    void odrzucaNiedodatniaKwote() {
        assertThatThrownBy(() -> krol.przekup(posel, 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ustawBudzetNadpisujeSkarbiec() {
        krol.przekup(posel, 40);
        krol.ustawBudzet(120);

        assertThat(krol.budzet()).isEqualTo(120);
        assertThatThrownBy(() -> krol.ustawBudzet(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
