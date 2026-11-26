package pl.hubert.liberumveto.domain.exception;

import java.io.Serial;
import java.util.Objects;
import pl.hubert.liberumveto.domain.model.Deputy;
import pl.hubert.liberumveto.domain.sejm.VoteResult;

/**
 * A deputy shouted "Nie pozwalam!" ("I do not allow!") and the Sejm was dissolved.
 * Checked (extends Exception): it is a normal, expected event in the game, and the compiler forces
 * the caller to handle it deliberately.
 */
public class SejmDissolvedException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    // transient: these fields are skipped by Java serialization (Deputy is not Serializable)
    private final transient Deputy vetoer;
    private final transient VoteResult result;

    public SejmDissolvedException(Deputy vetoer, VoteResult result) {
        super("Sejm zerwany przez: " + vetoer.fullName());
        this.vetoer = Objects.requireNonNull(vetoer, "vetoer");
        this.result = Objects.requireNonNull(result, "result");
    }

    public Deputy vetoer() {
        return vetoer;
    }

    /** The full vote result, so we can still show how people voted despite the dissolution. */
    public VoteResult result() {
        return result;
    }
}
