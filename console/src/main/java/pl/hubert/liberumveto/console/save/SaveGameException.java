package pl.hubert.liberumveto.console.save;

import java.io.Serial;

/**
 * The game state could not be saved or loaded (missing file, no permissions, corrupted JSON).
 * Checked: file problems happen even in a correct program, so the caller has to decide
 * consciously what to do about them.
 */
public class SaveGameException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public SaveGameException(String message, Throwable cause) {
        super(message, cause);
    }
}
