package pl.hubert.sejmik.domain.game;

/**
 * The final outcome of the whole game (spec 3.2).
 */
public enum GameResult {
    KING_WINS("Wygrana Króla! Reformy przeszły, Rzeczpospolita rośnie w siłę."),
    MAGNATES_WIN("Wygrana magnatów. Złota wolność szlachecka zwyciężyła... na razie."),
    DRAW("Remis: Rzeczpospolita trwa.");

    private final String description;

    GameResult(String description) {
        this.description = description;
    }

    /** Text shown to the player (in Polish). */
    public String description() {
        return description;
    }
}
