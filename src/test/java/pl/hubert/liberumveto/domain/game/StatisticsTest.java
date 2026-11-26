package pl.hubert.liberumveto.domain.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import pl.hubert.liberumveto.domain.model.Bill;
import pl.hubert.liberumveto.domain.model.BillType;
import pl.hubert.liberumveto.domain.model.Deputy;
import pl.hubert.liberumveto.domain.model.Faction;

class StatisticsTest {

    private static final Bill TAX = new Bill(1, "Podatek", BillType.TAX, Map.of(Faction.MAGNATE, -30));

    private final List<Deputy> deputies = List.of(
            new Deputy(1, "A", "A", Faction.ROYAL, 80, 0.2),
            new Deputy(2, "B", "B", Faction.ROYAL, 60, 0.9),
            new Deputy(3, "C", "C", Faction.MAGNATE, 40, 0.5),   // 40 - 30 = 10 -> at risk
            new Deputy(4, "D", "D", Faction.MAGNATE, 20, 0.9),   // 20 - 30 -> 0 -> at risk
            new Deputy(5, "E", "E", Faction.INDEPENDENT, 50, 0.7));

    private final Statistics statistics = new Statistics(deputies);

    @Test
    void computesAverageLoyaltyPerFaction() {
        assertThat(statistics.averageLoyaltyByFaction())
                .containsExactly(
                        Map.entry(Faction.ROYAL, 70.0),
                        Map.entry(Faction.MAGNATE, 30.0),
                        Map.entry(Faction.INDEPENDENT, 50.0));
    }

    @Test
    void returnsTopThreeMostCorruptible() {
        // the 0.9 tie is broken by the lower id
        assertThat(statistics.mostCorruptible(3))
                .extracting(Deputy::id)
                .containsExactly(2, 4, 5);
    }

    @Test
    void listsDeputiesAtRiskOfVetoWorstFirst() {
        assertThat(statistics.atRiskOfVeto(TAX))
                .extracting(Deputy::id)
                .containsExactly(4, 3);
    }

    @Test
    void computesFactionSizesAndAverageSupport() {
        assertThat(statistics.factionSizes())
                .containsEntry(Faction.ROYAL, 2L)
                .containsEntry(Faction.MAGNATE, 2L)
                .containsEntry(Faction.INDEPENDENT, 1L);
        // (80 + 60 + 10 + 0 + 50) / 5 = 40
        assertThat(statistics.averageSupport(TAX)).isCloseTo(40.0, within(0.001));
    }

    @Test
    void emptySejmGivesEmptyStatistics() {
        var empty = new Statistics(List.of());

        assertThat(empty.averageLoyaltyByFaction()).isEmpty();
        assertThat(empty.mostCorruptible(3)).isEmpty();
        assertThat(empty.averageSupport(TAX)).isZero();
    }
}
