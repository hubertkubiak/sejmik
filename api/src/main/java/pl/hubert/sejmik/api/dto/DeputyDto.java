package pl.hubert.sejmik.api.dto;

import pl.hubert.sejmik.domain.model.Bill;
import pl.hubert.sejmik.domain.model.Deputy;
import pl.hubert.sejmik.domain.model.Faction;

/**
 * A deputy as seen by API clients. Bribe amounts stay hidden; the client sees their effect (support).
 *
 * @param support           support for the selected bill (0-100), or null when no bill is selected
 * @param vetoChancePercent chance of a veto in percent, or null when no bill is selected
 */
public record DeputyDto(
        int id,
        String name,
        String coatOfArms,
        String fullName,
        Faction faction,
        int loyaltyToKing,
        double corruptibility,
        Integer support,
        Integer vetoChancePercent) {

    public static DeputyDto from(Deputy deputy, Bill selectedBill) {
        Integer support = selectedBill == null ? null : deputy.calculateSupport(selectedBill);
        Integer vetoChance = support == null ? null : vetoChancePercent(support);
        return new DeputyDto(deputy.id(), deputy.name(), deputy.coatOfArms(), deputy.fullName(),
                deputy.faction(), deputy.loyaltyToKing(), deputy.corruptibility(), support, vetoChance);
    }

    private static int vetoChancePercent(int support) {
        return support >= Deputy.VETO_THRESHOLD
                ? 0
                : (Deputy.VETO_THRESHOLD - support) * 100 / Deputy.VETO_THRESHOLD;
    }
}
