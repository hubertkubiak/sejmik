package pl.hubert.sejmik.api.service;

import java.io.Serial;
import java.util.UUID;

/**
 * No game with the given id. {@link pl.hubert.sejmik.api.error.ApiExceptionHandler} turns it into 404.
 */
public class GameNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public GameNotFoundException(UUID id) {
        super("Nie ma gry o id " + id);
    }
}
