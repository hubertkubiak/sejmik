package pl.hubert.liberumveto.domain.game;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import pl.hubert.liberumveto.domain.model.Bill;
import pl.hubert.liberumveto.domain.model.Deputy;
import pl.hubert.liberumveto.domain.model.Faction;

/**
 * Sejm statistics (F7), computed entirely with the Stream API.
 * An immutable object: it takes a snapshot of the deputy list and only reads from it.
 */
public class Statistics {

    private final List<Deputy> deputies;

    public Statistics(List<Deputy> deputies) {
        this.deputies = List.copyOf(deputies);
    }

    /** Average loyalty to the King in each faction (factions without deputies are omitted). */
    public Map<Faction, Double> averageLoyaltyByFaction() {
        return deputies.stream()
                .collect(Collectors.groupingBy(
                        Deputy::faction,
                        () -> new EnumMap<>(Faction.class),
                        Collectors.averagingInt(Deputy::loyaltyToKing)));
    }

    /** The {@code count} most corruptible deputies, most corruptible first. */
    public List<Deputy> mostCorruptible(int count) {
        return deputies.stream()
                .sorted(Comparator.comparingDouble(Deputy::corruptibility).reversed()
                        .thenComparingInt(Deputy::id))
                .limit(count)
                .toList();
    }

    /** Deputies who may cast a veto on the given bill, most endangered first. */
    public List<Deputy> atRiskOfVeto(Bill bill) {
        return deputies.stream()
                .filter(deputy -> deputy.calculateSupport(bill) < Deputy.VETO_THRESHOLD)
                .sorted(Comparator.comparingInt((Deputy deputy) -> deputy.calculateSupport(bill))
                        .thenComparingInt(Deputy::id))
                .toList();
    }

    /** How many deputies of each faction sit in the Sejm. */
    public Map<Faction, Long> factionSizes() {
        return deputies.stream()
                .collect(Collectors.groupingBy(Deputy::faction, () -> new EnumMap<>(Faction.class),
                        Collectors.counting()));
    }

    /** Average support for the bill across the whole Sejm. */
    public double averageSupport(Bill bill) {
        return deputies.stream()
                .mapToInt(deputy -> deputy.calculateSupport(bill))
                .average()
                .orElse(0.0);
    }
}
