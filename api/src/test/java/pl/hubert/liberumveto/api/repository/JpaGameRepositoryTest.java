package pl.hubert.liberumveto.api.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;
import pl.hubert.liberumveto.api.repository.entity.GameEntity;
import pl.hubert.liberumveto.domain.game.BillCatalog;
import pl.hubert.liberumveto.domain.game.DeputyGenerator;
import pl.hubert.liberumveto.domain.game.Game;

@SpringBootTest
class JpaGameRepositoryTest {

    @Autowired
    private GameRepository repository;

    @Autowired
    private GameEntityRepository entities;

    @Autowired
    private DeputyGenerator generator;

    @Autowired
    private BillCatalog catalog;

    @Autowired
    private TransactionTemplate transactions;

    /** Two Sejms played, the third one with a selected bill and bribes. */
    private Game gameInProgress() {
        var game = Game.start(generator, catalog, new Random(5));
        for (int i = 0; i < 2; i++) {
            game.selectBill(1);
            game.bribe(1, 10);
            game.vote();
        }
        game.selectBill(2);
        game.bribe(3, 25);
        return game;
    }

    @Test
    void savedGameLoadsWithIdenticalState() {
        UUID id = UUID.randomUUID();
        Game original = gameInProgress();

        repository.save(id, original);
        Game loaded = repository.findById(id).orElseThrow();

        assertThat(loaded.state()).usingRecursiveComparison().isEqualTo(original.state());
    }

    @Test
    void savingAgainUpdatesTheSameRowAndAppendsHistory() {
        UUID id = UUID.randomUUID();
        Game game = gameInProgress();
        repository.save(id, game);

        game.vote();
        repository.save(id, game);

        assertThat(repository.findById(id).orElseThrow().state().history()).hasSize(3);
        assertThat(entities.count()).isPositive();
        assertThat(entities.findById(id).orElseThrow().getVersion()).isEqualTo(1L);
    }

    @Test
    void unknownIdIsEmpty() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }

    /**
     * Simulates two requests: both read version 0, the first one commits, the second one's
     * UPDATE ... WHERE version = 0 matches nothing and fails instead of overwriting the first change.
     */
    @Test
    void optimisticLockingRejectsStaleUpdate() {
        UUID id = UUID.randomUUID();
        repository.save(id, gameInProgress());

        GameEntity stale = entities.findById(id).orElseThrow();
        transactions.executeWithoutResult(status -> {
            GameEntity fresh = entities.findById(id).orElseThrow();
            fresh.setKingBudget(1);
        });
        stale.setKingBudget(2);

        assertThatThrownBy(() -> entities.save(stale)).isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
