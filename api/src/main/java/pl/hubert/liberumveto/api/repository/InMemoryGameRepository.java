package pl.hubert.liberumveto.api.repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import pl.hubert.liberumveto.domain.game.Game;

/**
 * Games kept in memory; they disappear when the application restarts. A temporary solution
 * until the JPA step.
 *
 * <p>A web server handles many requests at once, each on its own thread, so a plain
 * {@code HashMap} could get corrupted. {@code ConcurrentHashMap} is safe for concurrent use.
 */
@Repository
public class InMemoryGameRepository implements GameRepository {

    private final Map<UUID, Game> games = new ConcurrentHashMap<>();

    @Override
    public void save(UUID id, Game game) {
        games.put(id, game);
    }

    @Override
    public Optional<Game> findById(UUID id) {
        return Optional.ofNullable(games.get(id));
    }
}
