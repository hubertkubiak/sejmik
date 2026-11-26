package pl.hubert.sejmik.domain.sejm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;
import pl.hubert.sejmik.domain.exception.SejmDissolvedException;
import pl.hubert.sejmik.domain.model.Bill;
import pl.hubert.sejmik.domain.model.BillType;
import pl.hubert.sejmik.domain.model.Deputy;
import pl.hubert.sejmik.domain.model.Faction;
import pl.hubert.sejmik.domain.model.FixedRandom;
import pl.hubert.sejmik.domain.model.Vote;

class SejmTest {

    private static final Bill BILL = new Bill(1, "Podatek", BillType.TAX, Map.of());
    private static final Random UNLUCKY = new FixedRandom(0.0);

    private static Deputy deputy(int id, int support) {
        return new Deputy(id, "Posel" + id, "Herb", Faction.INDEPENDENT, support, 0.5);
    }

    @Test
    void billPassesWithMajority() throws SejmDissolvedException {
        var sejm = new Sejm(List.of(deputy(1, 80), deputy(2, 60), deputy(3, 30)), UNLUCKY);

        VoteResult result = sejm.deliberate(BILL);

        assertThat(result.passed()).isTrue();
        assertThat(result.dissolved()).isFalse();
        assertThat(result.count(Vote.FOR)).isEqualTo(2);
        assertThat(result.count(Vote.AGAINST)).isEqualTo(1);
        assertThat(result.count(Vote.VETO)).isZero();
    }

    @Test
    void billFailsOnTie() throws SejmDissolvedException {
        var sejm = new Sejm(List.of(deputy(1, 80), deputy(2, 30)), UNLUCKY);

        assertThat(sejm.deliberate(BILL).passed()).isFalse();
    }

    @Test
    void singleVetoDissolvesSejm() {
        var sejm = new Sejm(List.of(deputy(1, 90), deputy(2, 90), deputy(3, 90), deputy(4, 5)), UNLUCKY);

        SejmDissolvedException exception = assertThrows(SejmDissolvedException.class, () -> sejm.deliberate(BILL));

        assertThat(exception.vetoer().id()).isEqualTo(4);
        assertThat(exception.result().passed()).isFalse();
        assertThat(exception.result().count(Vote.FOR)).isEqualTo(3);
        assertThat(exception.result().count(Vote.VETO)).isEqualTo(1);
        assertThat(exception.getMessage()).contains("Posel4 h. Herb");
    }

    @Test
    void vetoerIsTheFirstDeputyToVeto() {
        var sejm = new Sejm(List.of(deputy(1, 90), deputy(2, 3), deputy(3, 1)), UNLUCKY);

        assertThatThrownBy(() -> sejm.deliberate(BILL))
                .isInstanceOfSatisfying(SejmDissolvedException.class,
                        e -> assertThat(e.vetoer().id()).isEqualTo(2));
    }

    @Test
    void resultAlwaysContainsAllVoteKinds() {
        var result = new VoteResult(Map.of(Vote.FOR, 3L), true, Optional.empty());

        assertThat(result.voteCounts()).containsOnlyKeys(Vote.values());
        assertThat(result.count(Vote.AGAINST)).isZero();
    }

    @Test
    void sejmWithoutDeputiesCannotDeliberate() {
        assertThatThrownBy(() -> new Sejm(List.of(), UNLUCKY)).isInstanceOf(IllegalArgumentException.class);
    }
}
