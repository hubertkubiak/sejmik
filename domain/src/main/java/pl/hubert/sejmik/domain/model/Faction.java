package pl.hubert.sejmik.domain.model;

/**
 * The political faction a deputy belongs to.
 */
public enum Faction {
    ROYAL("królewska"),
    MAGNATE("magnacka"),
    INDEPENDENT("niezależna");

    private final String displayName;

    Faction(String displayName) {
        this.displayName = displayName;
    }

    /** Name shown to the player (in Polish). */
    public String displayName() {
        return displayName;
    }
}
