package pl.hubert.liberumveto.domain.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import pl.hubert.liberumveto.domain.model.Deputy;

class GeneratorAndCatalogTest {

    private static final List<String> NAMES = List.of("Jan Zamoyski", "Stanisław Radziwiłł");
    private static final List<String> COATS_OF_ARMS = List.of("Jelita", "Trąby");

    @Test
    void generatesDeputiesWithValidAttributesAndSequentialIds() {
        List<Deputy> deputies = new DeputyGenerator(NAMES, COATS_OF_ARMS, new Random(7)).generate(50);

        assertThat(deputies).hasSize(50)
                .extracting(Deputy::id).containsExactlyElementsOf(IntStream.rangeClosed(1, 50).boxed().toList());
        assertThat(deputies).allSatisfy(deputy -> {
            assertThat(deputy.loyaltyToKing()).isBetween(0, 100);
            assertThat(deputy.corruptibility()).isBetween(0.2, 1.0);
            assertThat(NAMES).contains(deputy.name());
            assertThat(COATS_OF_ARMS).contains(deputy.coatOfArms());
        });
    }

    @Test
    void sameSeedGivesSameDeputies() {
        var first = new DeputyGenerator(NAMES, COATS_OF_ARMS, new Random(123)).generate(20);
        var second = new DeputyGenerator(NAMES, COATS_OF_ARMS, new Random(123)).generate(20);

        assertThat(first).usingRecursiveFieldByFieldElementComparator().isEqualTo(second);
    }

    @Test
    void generatorRequiresNamesAndCoatsOfArms() {
        assertThatThrownBy(() -> new DeputyGenerator(List.of(), COATS_OF_ARMS, new Random()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void catalogDrawsDistinctBillsFromThePool() {
        var catalog = BillCatalog.defaultCatalog();

        var drawn = catalog.draw(3, new Random(5));

        assertThat(drawn).hasSize(3).doesNotHaveDuplicates();
        assertThat(catalog.all()).containsAll(drawn);
        assertThatThrownBy(() -> catalog.draw(99, new Random()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
