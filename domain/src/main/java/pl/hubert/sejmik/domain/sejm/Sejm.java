package pl.hubert.sejmik.domain.sejm;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import pl.hubert.sejmik.domain.exception.SejmDissolvedException;
import pl.hubert.sejmik.domain.model.Bill;
import pl.hubert.sejmik.domain.model.Deputy;
import pl.hubert.sejmik.domain.model.Vote;

/**
 * The Sejm (parliament): holds a single vote on a bill.
 */
public class Sejm {

    private final List<Deputy> deputies;
    private final Random random;

    public Sejm(List<Deputy> deputies, Random random) {
        this.deputies = List.copyOf(deputies);
        this.random = Objects.requireNonNull(random, "random");
        if (this.deputies.isEmpty()) {
            throw new IllegalArgumentException("A Sejm without deputies cannot deliberate");
        }
    }

    /**
     * Every deputy casts a vote (the list order matters for the random draws).
     * The bill passes when there are more FOR votes than AGAINST. A single veto dissolves the Sejm.
     *
     * @throws SejmDissolvedException when at least one deputy cast a veto
     */
    public VoteResult deliberate(Bill bill) throws SejmDissolvedException {
        Objects.requireNonNull(bill, "bill");

        // LinkedHashMap keeps the order, so the "first vetoer" is unambiguous
        Map<Deputy, Vote> votes = deputies.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        deputy -> deputy.vote(bill, random),
                        (a, b) -> a,
                        LinkedHashMap::new));

        Map<Vote, Long> voteCounts = votes.values().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Optional<Deputy> vetoer = votes.entrySet().stream()
                .filter(entry -> entry.getValue() == Vote.VETO)
                .map(Map.Entry::getKey)
                .findFirst();

        if (vetoer.isPresent()) {
            var result = new VoteResult(voteCounts, false, vetoer);
            throw new SejmDissolvedException(vetoer.get(), result);
        }

        long votesFor = voteCounts.getOrDefault(Vote.FOR, 0L);
        long votesAgainst = voteCounts.getOrDefault(Vote.AGAINST, 0L);
        return new VoteResult(voteCounts, votesFor > votesAgainst, Optional.empty());
    }
}
