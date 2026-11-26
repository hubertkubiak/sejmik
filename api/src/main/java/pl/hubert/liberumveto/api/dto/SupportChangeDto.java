package pl.hubert.liberumveto.api.dto;

import pl.hubert.liberumveto.domain.game.SupportChange;

/**
 * The effect of the King's bribe and what is left in the treasury.
 */
public record SupportChangeDto(DeputyDto deputy, int supportBefore, int supportAfter, int kingBudget) {

    public static SupportChangeDto from(SupportChange change, DeputyDto deputy, int kingBudget) {
        return new SupportChangeDto(deputy, change.before(), change.after(), kingBudget);
    }
}
