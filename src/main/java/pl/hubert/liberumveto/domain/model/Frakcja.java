package pl.hubert.liberumveto.domain.model;

/**
 * Stronnictwo, do którego należy poseł.
 */
public enum Frakcja {
    KROLEWSKA("królewska"),
    MAGNACKA("magnacka"),
    NIEZALEZNA("niezależna");

    private final String nazwa;

    Frakcja(String nazwa) {
        this.nazwa = nazwa;
    }

    /** Nazwa do wyświetlenia (z polskimi znakami). */
    public String nazwa() {
        return nazwa;
    }
}
