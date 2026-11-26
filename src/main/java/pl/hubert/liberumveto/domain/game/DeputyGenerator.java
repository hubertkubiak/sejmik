package pl.hubert.liberumveto.domain.game;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;
import pl.hubert.liberumveto.domain.model.Deputy;
import pl.hubert.liberumveto.domain.model.Faction;

/**
 * Creates deputies with random attributes (F2).
 * It does not read files: it receives ready-made names and coats of arms, so the domain has no I/O.
 */
public class DeputyGenerator {

    private final List<String> names;
    private final List<String> coatsOfArms;
    private final Random random;

    public DeputyGenerator(List<String> names, List<String> coatsOfArms, Random random) {
        if (names.isEmpty() || coatsOfArms.isEmpty()) {
            throw new IllegalArgumentException("At least one name and one coat of arms are required");
        }
        this.names = List.copyOf(names);
        this.coatsOfArms = List.copyOf(coatsOfArms);
        this.random = Objects.requireNonNull(random, "random");
    }

    public List<Deputy> generate(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(this::newDeputy)
                .toList();
    }

    private Deputy newDeputy(int id) {
        Faction faction = drawFaction();
        // switch as an expression (Java 14+): every branch yields a value, and the compiler
        // checks that all enum constants are covered
        int loyalty = switch (faction) {
            case ROYAL -> drawFromRange(55, 85);
            case INDEPENDENT -> drawFromRange(35, 65);
            case MAGNATE -> drawFromRange(20, 45);
        };
        double corruptibility = Math.round(random.nextDouble(0.2, 1.0) * 100) / 100.0;
        return new Deputy(id, drawFrom(names), drawFrom(coatsOfArms), faction, loyalty, corruptibility);
    }

    private Faction drawFaction() {
        int roll = random.nextInt(100);
        if (roll < 35) {
            return Faction.ROYAL;
        }
        if (roll < 65) {
            return Faction.INDEPENDENT;
        }
        return Faction.MAGNATE;
    }

    private int drawFromRange(int fromInclusive, int toInclusive) {
        return random.nextInt(fromInclusive, toInclusive + 1);
    }

    private String drawFrom(List<String> list) {
        return list.get(random.nextInt(list.size()));
    }
}
