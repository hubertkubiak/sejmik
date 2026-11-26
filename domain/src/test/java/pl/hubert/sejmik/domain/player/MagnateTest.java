package pl.hubert.sejmik.domain.player;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import pl.hubert.sejmik.domain.model.Bill;
import pl.hubert.sejmik.domain.model.BillType;
import pl.hubert.sejmik.domain.model.Deputy;
import pl.hubert.sejmik.domain.model.Faction;

class MagnateTest {

    private static final Bill BILL = new Bill(1, "Podatek", BillType.TAX, Map.of());

    @Test
    void bribesCheapestDeputyFirstAndPushesThemToZero() {
        var loyal = new Deputy(1, "A", "A", Faction.ROYAL, 80, 0.5);
        var wavering = new Deputy(2, "B", "B", Faction.INDEPENDENT, 20, 1.0);
        var magnate = new Magnate("Radziwiłł", 25);

        Map<Deputy, Integer> bribes = magnate.distributeBribes(List.of(loyal, wavering), BILL);

        // cost for the wavering deputy: ceil((20 + 0.5) / 1.0) = 21; the remaining 4 ducats go to the loyal one
        assertThat(bribes).containsExactly(Map.entry(wavering, 21), Map.entry(loyal, 4));
        assertThat(wavering.calculateSupport(BILL)).isZero();
        assertThat(magnate.budget()).isZero();
    }

    @Test
    void skipsIncorruptibleDeputiesAndThoseAlreadyAtZero() {
        var incorruptible = new Deputy(1, "A", "A", Faction.ROYAL, 10, 0.0);
        var atZero = new Deputy(2, "B", "B", Faction.MAGNATE, 0, 0.8);
        var magnate = new Magnate("Radziwiłł", 50);

        assertThat(magnate.distributeBribes(List.of(incorruptible, atZero), BILL)).isEmpty();
        assertThat(magnate.budget()).isEqualTo(50);
    }

    @Test
    void costTakesCorruptibilityIntoAccount() {
        var deputy = new Deputy(1, "A", "A", Faction.INDEPENDENT, 30, 0.5);

        // ceil(30.5 / 0.5) = 61
        assertThat(Magnate.costToZero(deputy, BILL)).isEqualTo(61);
    }
}
