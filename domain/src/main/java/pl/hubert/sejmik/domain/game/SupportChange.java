package pl.hubert.sejmik.domain.game;

import pl.hubert.sejmik.domain.model.Deputy;

/**
 * The effect of the King's bribe, e.g. "Jan Zamoyski h. Jelita: poparcie 12 → 34".
 */
public record SupportChange(Deputy deputy, int before, int after) {
}
