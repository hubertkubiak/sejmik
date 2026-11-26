package pl.hubert.sejmik.domain.model;

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

class DeputyTest {

    private static final Bill NEUTRAL = new Bill(1, "Neutralna", BillType.TAX, Map.of());
    private static final Random ALWAYS_VETO = new FixedRandom(0.0);
    private static final Random NEVER_VETO = new FixedRandom(0.999);

    private static Deputy deputyWithLoyalty(int loyalty) {
        return new Deputy(1, "Jan Zamoyski", "Jelita", Faction.INDEPENDENT, loyalty, 0.5);
    }

    @Nested
    @DisplayName("Decision at the threshold boundaries")
    class Thresholds {

        @ParameterizedTest(name = "support {0} -> FOR")
        @ValueSource(ints = {50, 51, 100})
        void votesForFromFifty(int support) {
            assertThat(deputyWithLoyalty(support).vote(NEUTRAL, ALWAYS_VETO)).isEqualTo(Vote.FOR);
        }

        @ParameterizedTest(name = "support {0} -> AGAINST")
        @ValueSource(ints = {15, 16, 49})
        void votesAgainstBetweenThresholds(int support) {
            // even an "unlucky" random draw cannot cause a veto at 15 or above
            assertThat(deputyWithLoyalty(support).vote(NEUTRAL, ALWAYS_VETO)).isEqualTo(Vote.AGAINST);
        }

        @ParameterizedTest(name = "support {0} -> VETO on an unlucky draw")
        @ValueSource(ints = {0, 7, 14})
        void vetoesBelowFifteenWhenTheDrawAllows(int support) {
            assertThat(deputyWithLoyalty(support).vote(NEUTRAL, ALWAYS_VETO)).isEqualTo(Vote.VETO);
        }

        @ParameterizedTest(name = "support {0} -> AGAINST on a lucky draw")
        @ValueSource(ints = {1, 7, 14})
        void votesAgainstBelowFifteenWhenTheDrawIsLucky(int support) {
            assertThat(deputyWithLoyalty(support).vote(NEUTRAL, NEVER_VETO)).isEqualTo(Vote.AGAINST);
        }

        @Test
        void vetoIsCertainAtZeroSupport() {
            // chance = (15 - 0) / 15 = 1.0, and nextDouble() < 1.0 always holds
            assertThat(deputyWithLoyalty(0).vote(NEUTRAL, NEVER_VETO)).isEqualTo(Vote.VETO);
        }

        @Test
        void vetoChanceGrowsLinearly() {
            // support 10 -> chance (15-10)/15 = 0.333...
            var deputy = deputyWithLoyalty(10);
            assertThat(deputy.vote(NEUTRAL, new FixedRandom(0.33))).isEqualTo(Vote.VETO);
            assertThat(deputy.vote(NEUTRAL, new FixedRandom(0.34))).isEqualTo(Vote.AGAINST);
        }
    }

    @Nested
    @DisplayName("Support algorithm")
    class Support {

        @ParameterizedTest(name = "loyalty {0}, impact {1}, king {2}, magnates {3}, corruptibility {4} -> {5}")
        @CsvSource({
                // loyalty, impact, kingBribes, magnateBribes, corruptibility, expected
                "40,   0,   0,   0, 0.5,  40",
                "40,  20,   0,   0, 0.5,  60",
                "40, -30,   0,   0, 0.5,  10",
                "40,   0,  30,   0, 0.5,  55",
                "40,   0,   0,  30, 0.5,  25",
                "40,   0,  30,  30, 1.0,  40",
                "90,  30,   0,   0, 0.5, 100",   // clamped from above
                "10, -30,   0,  50, 1.0,   0",   // clamped from below
                "20,   0,  10,   0, 0.0,  20",   // an incorruptible deputy ignores bribes
        })
        void calculatesSupportAccordingToFormula(int loyalty, int impact, int king, int magnates,
                                                 double corruptibility, int expected) {
            var bill = new Bill(2, "Test", BillType.TAX, Map.of(Faction.MAGNATE, impact));
            var deputy = new Deputy(1, "Jan", "Ogończyk", Faction.MAGNATE, loyalty, corruptibility, king, magnates);

            assertThat(deputy.calculateSupport(bill)).isEqualTo(expected);
        }

        @Test
        void billImpactDependsOnDeputyFaction() {
            var bill = new Bill(3, "Podatek", BillType.TAX, Map.of(Faction.ROYAL, 20, Faction.MAGNATE, -30));
            var royalist = new Deputy(1, "A", "B", Faction.ROYAL, 40, 0.5);
            var magnateMan = new Deputy(2, "C", "D", Faction.MAGNATE, 40, 0.5);

            assertThat(royalist.calculateSupport(bill)).isEqualTo(60);
            assertThat(magnateMan.calculateSupport(bill)).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Bribes")
    class Bribes {

        @Test
        void bribesAddUpAndCanBeCleared() {
            var deputy = deputyWithLoyalty(30);
            deputy.acceptKingBribe(10);
            deputy.acceptKingBribe(20);
            deputy.acceptMagnateBribe(5);

            assertThat(deputy.kingBribes()).isEqualTo(30);
            assertThat(deputy.magnateBribes()).isEqualTo(5);

            deputy.clearBribes();

            assertThat(deputy.kingBribes()).isZero();
            assertThat(deputy.magnateBribes()).isZero();
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -5})
        void rejectsNonPositiveAmount(int amount) {
            assertThatThrownBy(() -> deputyWithLoyalty(30).acceptKingBribe(amount))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void rejectsInvalidAttributes() {
        assertThatThrownBy(() -> deputyWithLoyalty(101)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Deputy(1, "A", "B", Faction.ROYAL, 50, 1.5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equalityIsBasedOnId() {
        var a = new Deputy(7, "Jan", "Jelita", Faction.ROYAL, 50, 0.5);
        var b = new Deputy(7, "Jan", "Jelita", Faction.ROYAL, 50, 0.5);
        b.acceptKingBribe(10);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a.fullName()).isEqualTo("Jan h. Jelita");
    }
}
