package pl.hubert.liberumveto.api.service;

import java.io.Serial;
import java.util.UUID;

/**
 * No game with the given id. {@link pl.hubert.liberumveto.api.error.ApiExceptionHandler} turns it into 404.
 */
public class GameNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public GameNotFoundException(UUID id) {
        super("Nie ma gry o id " + id);
    }
}
