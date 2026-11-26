package pl.hubert.sejmik.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BillTest {

    @Test
    void returnsImpactOnFactionOrZeroWhenMissing() {
        var bill = new Bill(1, "Podatek", BillType.TAX, Map.of(Faction.ROYAL, 20, Faction.MAGNATE, -30));

        assertThat(bill.impactOn(Faction.ROYAL)).isEqualTo(20);
        assertThat(bill.impactOn(Faction.MAGNATE)).isEqualTo(-30);
        assertThat(bill.impactOn(Faction.INDEPENDENT)).isZero();
    }

    @Test
    void staysImmutableEvenWhenOriginalMapChanges() {
        var map = new HashMap<Faction, Integer>();
        map.put(Faction.ROYAL, 10);
        var bill = new Bill(1, "Podatek", BillType.TAX, map);

        map.put(Faction.ROYAL, 99);

        assertThat(bill.impactOn(Faction.ROYAL)).isEqualTo(10);
        assertThatThrownBy(() -> bill.impact().put(Faction.MAGNATE, 1))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void recordsWithSameDataAreEqual() {
        var a = new Bill(1, "Podatek", BillType.TAX, Map.of());
        var b = new Bill(1, "Podatek", BillType.TAX, Map.of());

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }

    @Test
    void rejectsBlankTitle() {
        assertThatThrownBy(() -> new Bill(1, " ", BillType.TAX, Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
