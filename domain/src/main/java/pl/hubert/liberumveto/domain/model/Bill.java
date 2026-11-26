package pl.hubert.liberumveto.domain.model;

import java.util.Map;
import java.util.Objects;

/**
 * A bill put to the vote.
 *
 * @param id     unique identifier
 * @param title  name shown to the player
 * @param type   bill category
 * @param impact how much the bill shifts the support of each faction's deputies
 *               (e.g. ROYAL +20, MAGNATE -30)
 */
public record Bill(int id, String title, BillType type, Map<Faction, Integer> impact) {

    /**
     * Compact constructor: validation and a defensive copy. The compiler assigns the fields afterwards.
     */
    public Bill {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(type, "type");
        if (title.isBlank()) {
            throw new IllegalArgumentException("Bill title must not be blank");
        }
        // Map.copyOf creates an immutable copy, so nobody outside can change the bill's impact
        impact = Map.copyOf(Objects.requireNonNull(impact, "impact"));
    }

    /** The bill's impact on the given faction; 0 if the faction does not care. */
    public int impactOn(Faction faction) {
        return impact.getOrDefault(faction, 0);
    }
}
