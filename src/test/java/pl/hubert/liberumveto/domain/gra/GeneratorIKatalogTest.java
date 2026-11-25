package pl.hubert.liberumveto.domain.gra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import pl.hubert.liberumveto.domain.model.Posel;

class GeneratorIKatalogTest {

    private static final List<String> IMIONA = List.of("Jan Zamoyski", "Stanisław Radziwiłł");
    private static final List<String> HERBY = List.of("Jelita", "Trąby");

    @Test
    void generujePoslowZPoprawnymiAtrybutamiIKolejnymiId() {
        List<Posel> poslowie = new GeneratorPoslow(IMIONA, HERBY, new Random(7)).generuj(50);

        assertThat(poslowie).hasSize(50)
                .extracting(Posel::id).containsExactlyElementsOf(IntStream.rangeClosed(1, 50).boxed().toList());
        assertThat(poslowie).allSatisfy(posel -> {
            assertThat(posel.lojalnoscWobecKrola()).isBetween(0, 100);
            assertThat(posel.przekupnosc()).isBetween(0.2, 1.0);
            assertThat(IMIONA).contains(posel.imie());
            assertThat(HERBY).contains(posel.herb());
        });
    }

    @Test
    void toSamoZiarnoDajeTychSamychPoslow() {
        var pierwsi = new GeneratorPoslow(IMIONA, HERBY, new Random(123)).generuj(20);
        var drudzy = new GeneratorPoslow(IMIONA, HERBY, new Random(123)).generuj(20);

        assertThat(pierwsi).usingRecursiveFieldByFieldElementComparator().isEqualTo(drudzy);
    }

    @Test
    void generatorWymagaImionIHerbow() {
        assertThatThrownBy(() -> new GeneratorPoslow(List.of(), HERBY, new Random()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void katalogLosujeRozneUstawyZPuli() {
        var katalog = KatalogUstaw.domyslny();

        var wylosowane = katalog.losuj(3, new Random(5));

        assertThat(wylosowane).hasSize(3).doesNotHaveDuplicates();
        assertThat(katalog.wszystkie()).containsAll(wylosowane);
        assertThatThrownBy(() -> katalog.losuj(99, new Random()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
