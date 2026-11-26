package pl.hubert.liberumveto.api.service;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import pl.hubert.liberumveto.api.dto.BillDto;
import pl.hubert.liberumveto.api.dto.BillSelectionDto;
import pl.hubert.liberumveto.api.dto.DeputyDto;
import pl.hubert.liberumveto.api.dto.DeputySort;
import pl.hubert.liberumveto.api.dto.GameDto;
import pl.hubert.liberumveto.api.dto.SejmResultDto;
import pl.hubert.liberumveto.api.dto.SupportChangeDto;
import pl.hubert.liberumveto.api.dto.VoteResponseDto;
import pl.hubert.liberumveto.api.repository.GameRepository;
import pl.hubert.liberumveto.domain.exception.InvalidChoiceException;
import pl.hubert.liberumveto.domain.game.BillCatalog;
import pl.hubert.liberumveto.domain.game.DeputyGenerator;
import pl.hubert.liberumveto.domain.game.Game;
import pl.hubert.liberumveto.domain.game.SejmResult;
import pl.hubert.liberumveto.domain.game.SupportChange;
import pl.hubert.liberumveto.domain.model.Bill;
import pl.hubert.liberumveto.domain.model.Deputy;
import pl.hubert.liberumveto.domain.model.Faction;

/**
 * Application logic of the API: finds games, delegates the rules to the domain and maps the results
 * to DTOs. It knows nothing about HTTP (the controller's job) or how games are stored (the repository's).
 */
@Service
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

    public GameDto get(UUID id) {
        return withGame(id, game -> GameDto.from(id, game));
    }

    /**
     * @param faction only deputies of this faction, or null for all
     */
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

    public List<BillDto> availableBills(UUID id) {
        return withGame(id, game -> game.availableBills().stream().map(BillDto::from).toList());
    }

    /** The API selects bills by id; the domain expects a position on the list, so we translate. */
    public BillSelectionDto selectBill(UUID id, int billId) {
        return withGame(id, game -> {
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
        return withGame(id, game -> {
            SupportChange change = game.bribe(deputyId, amount);
            Bill bill = game.selectedBill().orElseThrow();
            return SupportChangeDto.from(change, DeputyDto.from(change.deputy(), bill),
                    game.state().king().budget());
        });
    }

    public VoteResponseDto vote(UUID id) {
        return withGame(id, game -> {
            SejmResult result = game.vote();
            return new VoteResponseDto(SejmResultDto.from(result), GameDto.from(id, game));
        });
    }

    public List<SejmResultDto> history(UUID id) {
        return withGame(id, game -> game.state().history().stream().map(SejmResultDto::from).toList());
    }

    /**
     * Runs an action on a game while holding its lock. Every request runs on its own thread, and
     * {@code Game} is not thread-safe: two simultaneous bribes could both pass the budget check.
     * {@code synchronized (game)} lets only one thread at a time touch a given game; different games
     * do not block each other.
     */
    private <T> T withGame(UUID id, Function<Game, T> action) {
        Game game = repository.findById(id).orElseThrow(() -> new GameNotFoundException(id));
        synchronized (game) {
            return action.apply(game);
        }
    }
}
