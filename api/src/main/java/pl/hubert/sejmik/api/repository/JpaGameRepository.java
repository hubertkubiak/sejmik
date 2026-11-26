package pl.hubert.sejmik.api.repository;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pl.hubert.sejmik.api.repository.entity.DeputyEntity;
import pl.hubert.sejmik.api.repository.entity.GameEntity;
import pl.hubert.sejmik.api.repository.entity.SejmResultEntity;
import pl.hubert.sejmik.domain.game.BillCatalog;
import pl.hubert.sejmik.domain.game.Game;
import pl.hubert.sejmik.domain.game.GameState;
import pl.hubert.sejmik.domain.game.SejmResult;
import pl.hubert.sejmik.domain.model.Bill;
import pl.hubert.sejmik.domain.model.Deputy;
import pl.hubert.sejmik.domain.model.Vote;
import pl.hubert.sejmik.domain.player.King;
import pl.hubert.sejmik.domain.player.Magnate;
import pl.hubert.sejmik.domain.sejm.VoteResult;

/**
 * {@link GameRepository} backed by a relational database. Translates between the domain model and
 * the JPA entities, so neither the domain nor the service knows about tables.
 *
 * <p>{@code @Transactional}: each method runs in a database transaction; if the caller (the service)
 * already has one, the method joins it.
 */
@Repository
@Transactional
public class JpaGameRepository implements GameRepository {

    private static final String KING_NAME = "Król";
    private static final String MAGNATE_NAME = "Magnaci";

    private final GameEntityRepository entities;
    private final Map<Integer, Bill> billsById;
    private final BillCatalog catalog;
    private final Random random;

    public JpaGameRepository(GameEntityRepository entities, BillCatalog catalog, Random random) {
        this.entities = entities;
        this.catalog = catalog;
        this.random = random;
        this.billsById = catalog.all().stream().collect(Collectors.toMap(Bill::id, Function.identity()));
    }

    /**
     * Inside one transaction {@code findById} returns the same managed entity that was loaded at the
     * start of the request, so we update it in place: Hibernate's dirty checking turns the changed
     * fields into an UPDATE at commit, guarded by the {@code @Version} check.
     */
    @Override
    public void save(UUID id, Game game) {
        GameEntity entity = entities.findById(id).orElseGet(() -> new GameEntity(id));
        GameState state = game.state();

        entity.setSejmNumber(state.sejmNumber());
        entity.setKingBudget(state.king().budget());
        entity.setMagnateBudget(state.magnate().budget());
        entity.setSelectedBillId(state.selectedBill().map(Bill::id).orElse(null));
        entity.setAvailableBillIds(state.availableBills().stream().map(Bill::id).toList());
        entity.setUpdatedAt(Instant.now());
        updateDeputies(entity, state.deputies());
        appendNewHistory(entity, state.history());

        entities.save(entity);
    }

    private static void updateDeputies(GameEntity entity, List<Deputy> deputies) {
        if (entity.getDeputies().isEmpty()) {
            deputies.forEach(d -> entity.addDeputy(new DeputyEntity(d.id(), d.name(), d.coatOfArms(),
                    d.faction(), d.loyaltyToKing(), d.corruptibility())));
        }
        // the set of deputies never changes during a game, only their bribes do
        Map<Integer, Deputy> byId = deputies.stream().collect(Collectors.toMap(Deputy::id, Function.identity()));
        for (DeputyEntity deputyEntity : entity.getDeputies()) {
            Deputy deputy = byId.get(deputyEntity.getDeputyId());
            deputyEntity.setBribes(deputy.kingBribes(), deputy.magnateBribes());
        }
    }

    /** History only ever grows, so we add just the Sejms the database does not have yet. */
    private static void appendNewHistory(GameEntity entity, List<SejmResult> history) {
        history.stream()
                .skip(entity.getHistory().size())
                .forEach(r -> entity.addSejmResult(new SejmResultEntity(
                        r.number(), r.bill().id(),
                        r.result().count(Vote.FOR), r.result().count(Vote.AGAINST), r.result().count(Vote.VETO),
                        r.result().passed(),
                        r.result().vetoer().map(Deputy::id).orElse(null))));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Game> findById(UUID id) {
        return entities.findById(id).map(this::toDomain);
    }

    private Game toDomain(GameEntity entity) {
        List<Deputy> deputies = entity.getDeputies().stream()
                .map(d -> new Deputy(d.getDeputyId(), d.getName(), d.getCoatOfArms(), d.getFaction(),
                        d.getLoyaltyToKing(), d.getCorruptibility(), d.getKingBribes(), d.getMagnateBribes()))
                .toList();
        Map<Integer, Deputy> deputiesById = deputies.stream()
                .collect(Collectors.toMap(Deputy::id, Function.identity()));

        List<SejmResult> history = entity.getHistory().stream()
                .map(r -> toDomain(r, deputiesById))
                .toList();

        var state = new GameState(
                entity.getSejmNumber(),
                deputies,
                new King(KING_NAME, entity.getKingBudget()),
                new Magnate(MAGNATE_NAME, entity.getMagnateBudget()),
                history,
                entity.getAvailableBillIds().stream().map(this::bill).toList(),
                entity.getSelectedBillId() == null ? null : bill(entity.getSelectedBillId()));
        return new Game(state, catalog, random);
    }

    private SejmResult toDomain(SejmResultEntity r, Map<Integer, Deputy> deputiesById) {
        Map<Vote, Long> counts = new EnumMap<>(Vote.class);
        counts.put(Vote.FOR, r.getVotesFor());
        counts.put(Vote.AGAINST, r.getVotesAgainst());
        counts.put(Vote.VETO, r.getVetoes());
        Optional<Deputy> vetoer = Optional.ofNullable(r.getVetoerDeputyId()).map(deputiesById::get);
        return new SejmResult(r.getNumber(), bill(r.getBillId()), new VoteResult(counts, r.isPassed(), vetoer));
    }

    private Bill bill(int id) {
        Bill bill = billsById.get(id);
        if (bill == null) {
            throw new IllegalStateException("Bill " + id + " from the database is not in the catalog");
        }
        return bill;
    }
}
