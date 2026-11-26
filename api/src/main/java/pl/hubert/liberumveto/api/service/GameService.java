package pl.hubert.liberumveto.api.service;

import java.util.Random;
import java.util.UUID;
import org.springframework.stereotype.Service;
import pl.hubert.liberumveto.api.repository.GameRepository;
import pl.hubert.liberumveto.domain.game.BillCatalog;
import pl.hubert.liberumveto.domain.game.DeputyGenerator;
import pl.hubert.liberumveto.domain.game.Game;

/**
 * Application logic of the API: finds and stores games and delegates the rules to the domain.
 * It knows nothing about HTTP (that is the controller's job) or about how games are stored
 * (that is the repository's job).
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

    public UUID create() {
        UUID id = UUID.randomUUID();
        repository.save(id, Game.start(deputyGenerator, billCatalog, random));
        return id;
    }

    public Game get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new GameNotFoundException(id));
    }
}
