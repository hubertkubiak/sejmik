package pl.hubert.sejmik.api.dto;

import java.util.UUID;
import pl.hubert.sejmik.domain.game.Game;
import pl.hubert.sejmik.domain.game.GameState;

/**
 * What the API returns about a game. A separate type from the domain: we decide exactly what goes
 * out (e.g. the magnates' budget stays secret) and the JSON does not change when the domain does.
 *
 * @param result the final outcome, or null while the game is in progress
 */
public record GameDto(
        UUID id,
        int sejmNumber,
        int sejmCount,
        int kingBudget,
        long passedCount,
        long dissolvedCount,
        boolean over,
        String result,
        BillDto selectedBill) {

    public static GameDto from(UUID id, Game game) {
        GameState state = game.state();
        return new GameDto(
                id,
                state.sejmNumber(),
                Game.SEJM_COUNT,
                state.king().budget(),
                game.passedCount(),
                game.dissolvedCount(),
                game.isOver(),
                game.isOver() ? game.result().name() : null,
                game.selectedBill().map(BillDto::from).orElse(null));
    }
}
