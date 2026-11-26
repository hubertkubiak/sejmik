package pl.hubert.sejmik.console.save;

import java.util.List;
import java.util.Map;
import pl.hubert.sejmik.domain.model.BillType;
import pl.hubert.sejmik.domain.model.Faction;
import pl.hubert.sejmik.domain.model.Vote;

/**
 * The save file format. Flat records with no logic (DTOs: Data Transfer Objects), kept separate
 * from the domain: changing domain classes does not break old saves, and the domain does not
 * depend on Jackson.
 *
 * @param version format version, so old files can be migrated in the future
 */
public record SavedGame(
        int version,
        int sejmNumber,
        List<DeputyDto> deputies,
        PlayerDto king,
        PlayerDto magnate,
        List<SejmResultDto> history,
        List<BillDto> availableBills,
        BillDto selectedBill) {

    public static final int CURRENT_VERSION = 1;

    public record DeputyDto(int id, String name, String coatOfArms, Faction faction, int loyaltyToKing,
                            double corruptibility, int kingBribes, int magnateBribes) {
    }

    public record PlayerDto(String name, int budget) {
    }

    public record BillDto(int id, String title, BillType type, Map<Faction, Integer> impact) {
    }

    /** @param vetoerId id of the deputy who dissolved the Sejm, or null */
    public record SejmResultDto(int number, BillDto bill, Map<Vote, Long> voteCounts,
                                boolean passed, Integer vetoerId) {
    }
}
