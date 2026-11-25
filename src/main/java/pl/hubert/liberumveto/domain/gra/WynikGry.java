package pl.hubert.liberumveto.domain.gra;

/**
 * Rozstrzygnięcie całej gry (specyfikacja 3.2).
 */
public enum WynikGry {
    WYGRANA_KROLA("Wygrana Króla! Reformy przeszły, Rzeczpospolita rośnie w siłę."),
    WYGRANA_MAGNATOW("Wygrana magnatów. Złota wolność szlachecka zwyciężyła... na razie."),
    REMIS("Remis: Rzeczpospolita trwa.");

    private final String opis;

    WynikGry(String opis) {
        this.opis = opis;
    }

    public String opis() {
        return opis;
    }
}
