package pl.hubert.sejmik.domain.game;

import java.util.Objects;
import pl.hubert.sejmik.domain.model.Bill;
import pl.hubert.sejmik.domain.sejm.VoteResult;

/**
 * A game history entry: which Sejm it was, what was voted on and how it ended.
 */
public record SejmResult(int number, Bill bill, VoteResult result) {

    public SejmResult {
        Objects.requireNonNull(bill, "bill");
        Objects.requireNonNull(result, "result");
    }
}
