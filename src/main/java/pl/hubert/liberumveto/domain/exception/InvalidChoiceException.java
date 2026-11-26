package pl.hubert.liberumveto.domain.exception;

import java.io.Serial;

/**
 * Invalid input from the player: an unknown command, a non-existent deputy, a wrong bill number.
 * Its message is shown to the player, so it is written in Polish.
 */
public class InvalidChoiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidChoiceException(String message) {
        super(message);
    }

    public InvalidChoiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
