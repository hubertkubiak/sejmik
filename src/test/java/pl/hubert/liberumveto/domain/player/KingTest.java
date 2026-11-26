package pl.hubert.liberumveto.domain.player;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.hubert.liberumveto.domain.exception.InsufficientBudgetException;
import pl.hubert.liberumveto.domain.model.Deputy;
import pl.hubert.liberumveto.domain.model.Faction;

class KingTest {

    private King king;
    private Deputy deputy;

    @BeforeEach
    void setUp() {
        // fresh objects before every test, so tests do not affect each other
        king = new King("Zygmunt", 100);
        deputy = new Deputy(7, "Jan Zamoyski", "Jelita", Faction.INDEPENDENT, 30, 0.5);
    }

    @Test
    void bribeReducesBudgetAndCountsAsKingBribe() {
        king.bribe(deputy, 30);

        assertThat(king.budget()).isEqualTo(70);
        assertThat(deputy.kingBribes()).isEqualTo(30);
        assertThat(deputy.magnateBribes()).isZero();
    }

    @Test
    void canSpendWholeBudget() {
        king.bribe(deputy, 100);

        assertThat(king.budget()).isZero();
    }

    @Test
    void exceedingBudgetThrowsAndChangesNothing() {
        assertThatThrownBy(() -> king.bribe(deputy, 101))
                .isInstanceOfSatisfying(InsufficientBudgetException.class,
                        e -> assertThat(e.amount()).isEqualTo(101))
                .hasMessageContaining("100");

        assertThat(king.budget()).isEqualTo(100);
        assertThat(deputy.kingBribes()).isZero();
    }

    @Test
    void rejectsNonPositiveAmount() {
        assertThatThrownBy(() -> king.bribe(deputy, 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void setBudgetOverwritesTreasury() {
        king.bribe(deputy, 40);
        king.setBudget(120);

        assertThat(king.budget()).isEqualTo(120);
        assertThatThrownBy(() -> king.setBudget(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
