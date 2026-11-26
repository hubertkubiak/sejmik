package pl.hubert.liberumveto.domain.model;

import java.util.Objects;
import java.util.Random;

/**
 * A deputy (poseł) in the Sejm. Their support changes with the bill and with bribes, so this is
 * a class rather than a record: it has an identity (id) and state that evolves over time.
 */
public class Deputy implements Voter {

    public static final int FOR_THRESHOLD = 50;
    public static final int VETO_THRESHOLD = 15;
    public static final int MIN_SUPPORT = 0;
    public static final int MAX_SUPPORT = 100;

    private final int id;
    private final String name;
    private final String coatOfArms;
    private final Faction faction;
    private final int loyaltyToKing;
    private final double corruptibility;
    private int kingBribes;
    private int magnateBribes;

    public Deputy(int id, String name, String coatOfArms, Faction faction,
                  int loyaltyToKing, double corruptibility) {
        this(id, name, coatOfArms, faction, loyaltyToKing, corruptibility, 0, 0);
    }

    /** Full constructor, useful e.g. when loading a saved game. */
    public Deputy(int id, String name, String coatOfArms, Faction faction,
                  int loyaltyToKing, double corruptibility,
                  int kingBribes, int magnateBribes) {
        if (loyaltyToKing < MIN_SUPPORT || loyaltyToKing > MAX_SUPPORT) {
            throw new IllegalArgumentException("Loyalty must be within 0-100: " + loyaltyToKing);
        }
        if (corruptibility < 0.0 || corruptibility > 1.0) {
            throw new IllegalArgumentException("Corruptibility must be within 0.0-1.0: " + corruptibility);
        }
        if (kingBribes < 0 || magnateBribes < 0) {
            throw new IllegalArgumentException("Bribes must not be negative");
        }
        this.id = id;
        this.name = Objects.requireNonNull(name, "name");
        this.coatOfArms = Objects.requireNonNull(coatOfArms, "coatOfArms");
        this.faction = Objects.requireNonNull(faction, "faction");
        this.loyaltyToKing = loyaltyToKing;
        this.corruptibility = corruptibility;
        this.kingBribes = kingBribes;
        this.magnateBribes = magnateBribes;
    }

    /**
     * The algorithm from the spec (3.3): loyalty + the bill's impact + king's bribes - magnates' bribes,
     * with bribes multiplied by corruptibility. The result is clamped to 0-100.
     */
    public int calculateSupport(Bill bill) {
        double raw = loyaltyToKing
                + bill.impactOn(faction)
                + kingBribes * corruptibility
                - magnateBribes * corruptibility;
        long rounded = Math.round(raw);
        return Math.clamp(rounded, MIN_SUPPORT, MAX_SUPPORT);
    }

    @Override
    public Vote vote(Bill bill, Random random) {
        int support = calculateSupport(bill);
        if (support >= FOR_THRESHOLD) {
            return Vote.FOR;
        }
        if (support >= VETO_THRESHOLD) {
            return Vote.AGAINST;
        }
        double vetoChance = (VETO_THRESHOLD - support) / (double) VETO_THRESHOLD;
        return random.nextDouble() < vetoChance ? Vote.VETO : Vote.AGAINST;
    }

    public void acceptKingBribe(int amount) {
        kingBribes += requirePositive(amount);
    }

    public void acceptMagnateBribe(int amount) {
        magnateBribes += requirePositive(amount);
    }

    /** Bribes only last for one Sejm; they are forgotten before the next one. */
    public void clearBribes() {
        kingBribes = 0;
        magnateBribes = 0;
    }

    private static int requirePositive(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Bribe amount must be positive: " + amount);
        }
        return amount;
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String coatOfArms() {
        return coatOfArms;
    }

    public Faction faction() {
        return faction;
    }

    public int loyaltyToKing() {
        return loyaltyToKing;
    }

    public double corruptibility() {
        return corruptibility;
    }

    public int kingBribes() {
        return kingBribes;
    }

    public int magnateBribes() {
        return magnateBribes;
    }

    /** E.g. "Jan Zamoyski h. Jelita" ("h." = "herbu", i.e. "of the coat of arms"). */
    public String fullName() {
        return name + " h. " + coatOfArms;
    }

    /** A deputy's identity is their id: their state (bribes) changes, but they stay the same deputy. */
    @Override
    public boolean equals(Object o) {
        return o instanceof Deputy other && id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Deputy[" + id + ", " + fullName() + ", " + faction + "]";
    }
}
