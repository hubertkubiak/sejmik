package pl.hubert.sejmik.domain.player;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import pl.hubert.sejmik.domain.model.Bill;
import pl.hubert.sejmik.domain.model.Deputy;

/**
 * The computer-controlled opponent. Their bribes lower support; the goal is at least one veto.
 */
public class Magnate extends Player {

    public Magnate(String name, int budget) {
        super(name, budget);
    }

    @Override
    protected void deliverBribe(Deputy deputy, int amount) {
        deputy.acceptMagnateBribe(amount);
    }

    /**
     * AI strategy: one veto is enough, so the magnate picks the deputies who are cheapest to push down
     * to zero support (where a veto is certain) and bribes them one by one while the ducats last.
     * Whatever is left goes to the next candidate, to at least raise the chance of a veto.
     *
     * @return who got how much (in the order of bribing)
     */
    public Map<Deputy, Integer> distributeBribes(List<Deputy> deputies, Bill bill) {
        List<Deputy> candidates = deputies.stream()
                .filter(deputy -> deputy.corruptibility() > 0)
                .filter(deputy -> deputy.calculateSupport(bill) > 0)
                .sorted(Comparator.comparingInt((Deputy deputy) -> costToZero(deputy, bill))
                        .thenComparingInt(Deputy::id))
                .toList();

        Map<Deputy, Integer> bribes = new LinkedHashMap<>();
        for (Deputy deputy : candidates) {
            if (budget() == 0) {
                break;
            }
            int amount = Math.min(costToZero(deputy, bill), budget());
            bribe(deputy, amount);
            bribes.put(deputy, amount);
        }
        // Map.copyOf would lose the order, so we wrap the LinkedHashMap in a read-only view
        return Collections.unmodifiableMap(bribes);
    }

    /**
     * How many ducats it takes to bring the deputy's support down to 0.
     * The +0.5 compensates for the rounding in {@link Deputy#calculateSupport(Bill)}.
     */
    static int costToZero(Deputy deputy, Bill bill) {
        return (int) Math.ceil((deputy.calculateSupport(bill) + 0.5) / deputy.corruptibility());
    }
}
