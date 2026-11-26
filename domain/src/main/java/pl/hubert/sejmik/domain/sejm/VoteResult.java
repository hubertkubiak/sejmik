package pl.hubert.sejmik.domain.sejm;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import pl.hubert.sejmik.domain.model.Deputy;
import pl.hubert.sejmik.domain.model.Vote;

/**
 * The outcome of a single vote.
 *
 * @param voteCounts how many votes of each kind were cast (always contains all three keys)
 * @param passed     whether the bill passed
 * @param vetoer     the deputy who dissolved the Sejm, if anyone did
 */
public record VoteResult(Map<Vote, Long> voteCounts, boolean passed, Optional<Deputy> vetoer) {

    public VoteResult {
        Objects.requireNonNull(voteCounts, "voteCounts");
        Objects.requireNonNull(vetoer, "vetoer");
        // fill missing vote kinds with zeros so callers never have to check for null
        var complete = new EnumMap<Vote, Long>(Vote.class);
        for (Vote vote : Vote.values()) {
            complete.put(vote, voteCounts.getOrDefault(vote, 0L));
        }
        voteCounts = Map.copyOf(complete);
        if (passed && vetoer.isPresent()) {
            throw new IllegalArgumentException("A dissolved Sejm cannot pass a bill");
        }
    }

    public long count(Vote vote) {
        return voteCounts.get(vote);
    }

    public boolean dissolved() {
        return vetoer.isPresent();
    }
}
