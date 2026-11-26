package pl.hubert.sejmik.domain.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pl.hubert.sejmik.domain.exception.InvalidChoiceException;
import pl.hubert.sejmik.domain.model.Bill;
import pl.hubert.sejmik.domain.model.BillType;
import pl.hubert.sejmik.domain.model.Deputy;
import pl.hubert.sejmik.domain.model.Faction;
import pl.hubert.sejmik.domain.player.King;
import pl.hubert.sejmik.domain.player.Magnate;

class GameTest {

    /** Bills no faction cares about, so support depends only on loyalty and bribes. */
    private static final BillCatalog NEUTRAL = new BillCatalog(IntStream.rangeClosed(1, 3)
            .mapToObj(i -> new Bill(i, "Ustawa " + i, BillType.TAX, Map.of()))
            .toList());

    /** A game with one "predictable" deputy: an incorruptible deputy always votes the same way. */
    private static Game gameWithDeputy(int loyalty, double corruptibility) {
        var deputy = new Deputy(1, "Jan", "Jelita", Faction.INDEPENDENT, loyalty, corruptibility);
        var random = new Random(42);
        var state = new GameState(1, List.of(deputy), new King("Król", 100), new Magnate("Magnaci", 160),
                List.of(), NEUTRAL.draw(Game.BILLS_TO_CHOOSE_FROM, random), null);
        return new Game(state, NEUTRAL, random);
    }

    private static void playTurn(Game game) {
        game.selectBill(1);
        game.vote();
    }

    @Nested
    class EndConditions {

        @Test
        void kingWinsWhenAtLeastThreeBillsPass() {
            var game = gameWithDeputy(90, 0.0);

            for (int i = 0; i < Game.SEJM_COUNT; i++) {
                playTurn(game);
            }

            assertThat(game.isOver()).isTrue();
            assertThat(game.passedCount()).isEqualTo(5);
            assertThat(game.result()).isEqualTo(GameResult.KING_WINS);
        }

        @Test
        void magnatesWinWhenAtLeastThreeSejmsAreDissolved() {
            var game = gameWithDeputy(0, 0.0); // support 0 -> a certain veto

            for (int i = 0; i < Game.SEJM_COUNT; i++) {
                playTurn(game);
            }

            assertThat(game.dissolvedCount()).isEqualTo(5);
            assertThat(game.result()).isEqualTo(GameResult.MAGNATES_WIN);
        }

        @Test
        void drawWhenBillsFailWithoutVeto() {
            var game = gameWithDeputy(30, 0.0); // always AGAINST, never a veto

            for (int i = 0; i < Game.SEJM_COUNT; i++) {
                playTurn(game);
            }

            assertThat(game.passedCount()).isZero();
            assertThat(game.dissolvedCount()).isZero();
            assertThat(game.result()).isEqualTo(GameResult.DRAW);
        }

        @Test
        void drawWithTwoPassedAndTwoDissolved() {
            // a deputy at support 0 is skipped by the magnates, so the King's bribe decides the vote
            var game = gameWithDeputy(0, 1.0);

            turnWithBribe(game, 50);  // support 50 -> FOR
            turnWithBribe(game, 60);  // FOR
            turnWithBribe(game, 0);   // support 0 -> VETO
            turnWithBribe(game, 0);   // VETO
            turnWithBribe(game, 30);  // AGAINST -> the bill fails

            assertThat(game.passedCount()).isEqualTo(2);
            assertThat(game.dissolvedCount()).isEqualTo(2);
            assertThat(game.result()).isEqualTo(GameResult.DRAW);
        }

        private void turnWithBribe(Game game, int amount) {
            game.selectBill(1);
            if (amount > 0) {
                game.bribe(1, amount);
            }
            game.vote();
        }

        @Test
        void resultIsUnavailableBeforeTheEnd() {
            assertThatThrownBy(() -> gameWithDeputy(90, 0.0).result()).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void cannotKeepPlayingAfterTheEnd() {
            var game = gameWithDeputy(90, 0.0);
            for (int i = 0; i < Game.SEJM_COUNT; i++) {
                playTurn(game);
            }

            assertThatThrownBy(() -> game.selectBill(1)).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(game::vote).isInstanceOf(IllegalStateException.class);
            assertThat(game.state().history()).hasSize(5)
                    .extracting(SejmResult::number).containsExactly(1, 2, 3, 4, 5);
        }
    }

    @Nested
    class TurnFlow {

        @Test
        void newGameHasTwentyDeputiesAndThreeBills() {
            var random = new Random(1);
            var game = Game.start(new DeputyGenerator(List.of("Jan"), List.of("Jelita"), random),
                    BillCatalog.defaultCatalog(), random);

            assertThat(game.state().deputies()).hasSize(Game.DEPUTY_COUNT);
            assertThat(game.availableBills()).hasSize(Game.BILLS_TO_CHOOSE_FROM).doesNotHaveDuplicates();
            assertThat(game.state().sejmNumber()).isEqualTo(1);
            assertThat(game.state().king().budget()).isEqualTo(Game.KING_BUDGET);
            assertThat(game.selectedBill()).isEmpty();
        }

        @Test
        void cannotVoteOrBribeBeforeSelectingBill() {
            var game = gameWithDeputy(50, 0.5);

            assertThatThrownBy(game::vote).isInstanceOf(InvalidChoiceException.class);
            assertThatThrownBy(() -> game.bribe(1, 10)).isInstanceOf(InvalidChoiceException.class);
        }

        @Test
        void cannotSelectBillTwiceOrOutsideTheList() {
            var game = gameWithDeputy(50, 0.5);

            assertThatThrownBy(() -> game.selectBill(0)).isInstanceOf(InvalidChoiceException.class);
            assertThatThrownBy(() -> game.selectBill(4)).isInstanceOf(InvalidChoiceException.class);

            game.selectBill(2);

            assertThat(game.selectedBill()).contains(game.availableBills().get(1));
            assertThatThrownBy(() -> game.selectBill(1)).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void selectingBillTriggersMagnateBribes() {
            var game = gameWithDeputy(40, 0.5);

            Map<Deputy, Integer> bribes = game.selectBill(1);

            assertThat(bribes).isNotEmpty();
            assertThat(game.state().deputies().getFirst().magnateBribes()).isPositive();
        }

        @Test
        void bribeReturnsSupportChange() {
            var game = gameWithDeputy(0, 1.0);
            game.selectBill(1);

            SupportChange change = game.bribe(1, 30);

            assertThat(change.before()).isZero();
            assertThat(change.after()).isEqualTo(30);
            assertThat(game.state().king().budget()).isEqualTo(70);
        }

        @Test
        void bribingUnknownDeputyThrows() {
            var game = gameWithDeputy(50, 0.5);
            game.selectBill(1);

            assertThatThrownBy(() -> game.bribe(99, 10))
                    .isInstanceOf(InvalidChoiceException.class)
                    .hasMessageContaining("99");
        }

        @Test
        void afterVoteNextSejmStartsWithNewBudgetsAndNoBribes() {
            var game = gameWithDeputy(0, 1.0);
            game.selectBill(1);
            game.bribe(1, 60); // FOR

            SejmResult result = game.vote();

            assertThat(result.result().passed()).isTrue();
            assertThat(game.state().sejmNumber()).isEqualTo(2);
            assertThat(game.selectedBill()).isEmpty();
            assertThat(game.state().deputies().getFirst().kingBribes()).isZero();
            assertThat(game.state().king().budget())
                    .isEqualTo(Game.KING_BUDGET + Game.KING_BONUS_PER_PASSED_BILL);
            assertThat(game.state().magnate().budget()).isEqualTo(Game.MAGNATE_BUDGET);
        }

        @Test
        void dissolvedSejmIsRecordedWithVetoer() {
            var game = gameWithDeputy(0, 0.0);
            game.selectBill(1);

            SejmResult result = game.vote();

            assertThat(result.result().dissolved()).isTrue();
            assertThat(result.result().vetoer()).map(Deputy::id).contains(1);
            assertThat(game.state().magnate().budget())
                    .isEqualTo(Game.MAGNATE_BUDGET + Game.MAGNATE_BONUS_PER_DISSOLUTION);
        }
    }
}
