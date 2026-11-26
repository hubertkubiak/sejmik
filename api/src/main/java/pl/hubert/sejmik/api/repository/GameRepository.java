package pl.hubert.sejmik.api.repository;

import java.util.Optional;
import java.util.UUID;
import pl.hubert.sejmik.domain.game.Game;

/**
 * Where games are kept. The service depends on this interface, not on a concrete storage, so
 * the in-memory version can later be replaced with a database one without touching the service.
 */
public interface GameRepository {

    void save(UUID id, Game game);

    Optional<Game> findById(UUID id);
}
