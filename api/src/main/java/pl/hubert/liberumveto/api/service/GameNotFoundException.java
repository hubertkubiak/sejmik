package pl.hubert.liberumveto.api.service;

import java.io.Serial;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * No game with the given id. {@code @ResponseStatus} tells Spring MVC to answer 404 instead of 500.
 * (A temporary shortcut: a global error handler with a consistent error format comes in a later step.)
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class GameNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public GameNotFoundException(UUID id) {
        super("Nie ma gry o id " + id);
    }
}
