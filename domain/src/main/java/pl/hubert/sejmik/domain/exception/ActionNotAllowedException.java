package pl.hubert.sejmik.domain.exception;

import java.io.Serial;

/**
 * The action is valid in general but not in the current state of the game: the game is over,
 * a bill has already been selected for this Sejm. The player can trigger it (e.g. through the API),
 * so the message is in Polish.
 *
 * <p>It extends {@link IllegalStateException}, the standard Java exception for "wrong state for
 * this call", so code that catches the general type keeps working.
 */
public class ActionNotAllowedException extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ActionNotAllowedException(String message) {
        super(message);
    }
}
