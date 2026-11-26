package pl.hubert.sejmik.api.service;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.hubert.sejmik.api.dto.BillDto;
import pl.hubert.sejmik.api.dto.BillSelectionDto;
import pl.hubert.sejmik.api.dto.DeputyDto;
import pl.hubert.sejmik.api.dto.DeputySort;
import pl.hubert.sejmik.api.dto.GameDto;
import pl.hubert.sejmik.api.dto.SejmResultDto;
import pl.hubert.sejmik.api.dto.SupportChangeDto;
import pl.hubert.sejmik.api.dto.VoteResponseDto;
import pl.hubert.sejmik.api.repository.GameRepository;
import pl.hubert.sejmik.domain.exception.InvalidChoiceException;
import pl.hubert.sejmik.domain.game.BillCatalog;
import pl.hubert.sejmik.domain.game.DeputyGenerator;
import pl.hubert.sejmik.domain.game.Game;
import pl.hubert.sejmik.domain.game.SejmResult;
import pl.hubert.sejmik.domain.game.SupportChange;
import pl.hubert.sejmik.domain.model.Bill;
import pl.hubert.sejmik.domain.model.Deputy;
import pl.hubert.sejmik.domain.model.Faction;

/**
 * Application logic of the API: finds games, delegates the rules to the domain and maps the results
 * to DTOs. It knows nothing about HTTP (the controller's job) or how games are stored (the repository's).
 */
@Service
@Transactional
public class GameService {

    private final GameRepository repository;
    private final DeputyGenerator deputyGenerator;
    private final BillCatalog billCatalog;
    private final Random random;

    /**
     * Constructor injection: Spring finds beans of these types and passes them in.
     * With a single constructor no {@code @Autowired} is needed.
     */
    public GameService(GameRepository repository, DeputyGenerator deputyGenerator,
                       BillCatalog billCatalog, Random random) {
        this.repository = repository;
        this.deputyGenerator = deputyGenerator;
        this.billCatalog = billCatalog;
        this.random = random;
    }

    public GameDto create() {
        UUID id = UUID.randomUUID();
        Game game = Game.start(deputyGenerator, billCatalog, random);
        repository.save(id, game);
        return GameDto.from(id, game);
    }

    @Transactional(readOnly = true)
    public GameDto get(UUID id) {
        return withGame(id, game -> GameDto.from(id, game));
    }

    /**
     * @param faction only deputies of this faction, or null for all
     */
    @Transactional(readOnly = true)
    public List<DeputyDto> deputies(UUID id, Faction faction, DeputySort sort) {
        return withGame(id, game -> {
            Bill bill = game.selectedBill().orElse(null);
            if (sort == DeputySort.SUPPORT && bill == null) {
                throw new InvalidChoiceException("Sortowanie po poparciu wymaga wybranej ustawy");
            }
            return game.state().deputies().stream()
                    .filter(deputy -> faction == null || deputy.faction() == faction)
                    .sorted(comparator(sort, bill))
                    .map(deputy -> DeputyDto.from(deputy, bill))
                    .toList();
        });
    }

    private static Comparator<Deputy> comparator(DeputySort sort, Bill bill) {
        // switch expression over an enum: the compiler checks that every sort order is handled
        return switch (sort) {
            case ID -> Comparator.comparingInt(Deputy::id);
            case SUPPORT -> Comparator.comparingInt((Deputy d) -> d.calculateSupport(bill))
                    .thenComparingInt(Deputy::id);
            case CORRUPTIBILITY -> Comparator.comparingDouble(Deputy::corruptibility).reversed()
                    .thenComparingInt(Deputy::id);
        };
    }

    @Transactional(readOnly = true)
    public List<BillDto> availableBills(UUID id) {
        return withGame(id, game -> game.availableBills().stream().map(BillDto::from).toList());
    }

    /** The API selects bills by id; the domain expects a position on the list, so we translate. */
    public BillSelectionDto selectBill(UUID id, int billId) {
        return updateGame(id, game -> {
            List<Bill> bills = game.availableBills();
            int position = 1 + bills.stream().map(Bill::id).toList().indexOf(billId);
            if (position == 0) {
                throw new InvalidChoiceException("Ustawy o id " + billId + " nie ma na liście w tej turze");
            }
            int bribed = game.selectBill(position).size();
            return new BillSelectionDto(BillDto.from(bills.get(position - 1)), bribed);
        });
    }

    public SupportChangeDto bribe(UUID id, int deputyId, int amount) {
        return updateGame(id, game -> {
            SupportChange change = game.bribe(deputyId, amount);
            Bill bill = game.selectedBill().orElseThrow();
            return SupportChangeDto.from(change, DeputyDto.from(change.deputy(), bill),
                    game.state().king().budget());
        });
    }

    public VoteResponseDto vote(UUID id) {
        return updateGame(id, game -> {
            SejmResult result = game.vote();
            return new VoteResponseDto(SejmResultDto.from(result), GameDto.from(id, game));
        });
    }

    @Transactional(readOnly = true)
    public List<SejmResultDto> history(UUID id) {
        return withGame(id, game -> game.state().history().stream().map(SejmResultDto::from).toList());
    }

    /** Loads the game and runs a read-only action on it. */
    private <T> T withGame(UUID id, Function<Game, T> action) {
        return action.apply(load(id));
    }

    /**
     * Loads the game, runs an action that changes it and stores the result. All of it happens in the
     * one transaction of the calling service method.
     *
     * <p>Concurrency: every request loads its own copy of the game from the database, so two requests
     * for the same game no longer share an object to lock on (the earlier {@code synchronized}).
     * Instead the {@code @Version} column catches the conflict when the second one commits.
     */
    private <T> T updateGame(UUID id, Function<Game, T> action) {
        Game game = load(id);
        T result = action.apply(game);
        repository.save(id, game);
        return result;
    }

    private Game load(UUID id) {
        return repository.findById(id).orElseThrow(() -> new GameNotFoundException(id));
    }
}
