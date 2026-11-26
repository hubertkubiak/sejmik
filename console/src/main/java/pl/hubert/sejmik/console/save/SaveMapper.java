package pl.hubert.sejmik.console.save;

import java.util.List;
import java.util.Optional;
import pl.hubert.sejmik.console.save.SavedGame.BillDto;
import pl.hubert.sejmik.console.save.SavedGame.DeputyDto;
import pl.hubert.sejmik.console.save.SavedGame.PlayerDto;
import pl.hubert.sejmik.console.save.SavedGame.SejmResultDto;
import pl.hubert.sejmik.domain.game.GameState;
import pl.hubert.sejmik.domain.game.SejmResult;
import pl.hubert.sejmik.domain.model.Bill;
import pl.hubert.sejmik.domain.model.Deputy;
import pl.hubert.sejmik.domain.player.King;
import pl.hubert.sejmik.domain.player.Magnate;
import pl.hubert.sejmik.domain.sejm.VoteResult;

/**
 * Translates the domain model into save DTOs and back.
 */
final class SaveMapper {

    private SaveMapper() {
    }

    static SavedGame toDto(GameState state) {
        return new SavedGame(
                SavedGame.CURRENT_VERSION,
                state.sejmNumber(),
                state.deputies().stream().map(SaveMapper::toDto).toList(),
                new PlayerDto(state.king().name(), state.king().budget()),
                new PlayerDto(state.magnate().name(), state.magnate().budget()),
                state.history().stream().map(SaveMapper::toDto).toList(),
                state.availableBills().stream().map(SaveMapper::toDto).toList(),
                state.selectedBill().map(SaveMapper::toDto).orElse(null));
    }

    static GameState fromDto(SavedGame dto) {
        if (dto.version() != SavedGame.CURRENT_VERSION) {
            throw new IllegalArgumentException("Unsupported save version: " + dto.version());
        }
        List<Deputy> deputies = dto.deputies().stream().map(SaveMapper::fromDto).toList();
        return new GameState(
                dto.sejmNumber(),
                deputies,
                new King(dto.king().name(), dto.king().budget()),
                new Magnate(dto.magnate().name(), dto.magnate().budget()),
                dto.history().stream().map(result -> fromDto(result, deputies)).toList(),
                dto.availableBills().stream().map(SaveMapper::fromDto).toList(),
                dto.selectedBill() == null ? null : fromDto(dto.selectedBill()));
    }

    private static DeputyDto toDto(Deputy d) {
        return new DeputyDto(d.id(), d.name(), d.coatOfArms(), d.faction(), d.loyaltyToKing(),
                d.corruptibility(), d.kingBribes(), d.magnateBribes());
    }

    private static Deputy fromDto(DeputyDto d) {
        return new Deputy(d.id(), d.name(), d.coatOfArms(), d.faction(), d.loyaltyToKing(),
                d.corruptibility(), d.kingBribes(), d.magnateBribes());
    }

    private static BillDto toDto(Bill b) {
        return new BillDto(b.id(), b.title(), b.type(), b.impact());
    }

    private static Bill fromDto(BillDto b) {
        return new Bill(b.id(), b.title(), b.type(), b.impact());
    }

    private static SejmResultDto toDto(SejmResult r) {
        VoteResult result = r.result();
        return new SejmResultDto(r.number(), toDto(r.bill()), result.voteCounts(), result.passed(),
                result.vetoer().map(Deputy::id).orElse(null));
    }

    /** The vetoer is looked up among the loaded deputies, so the history points to the same objects. */
    private static SejmResult fromDto(SejmResultDto r, List<Deputy> deputies) {
        Optional<Deputy> vetoer = Optional.ofNullable(r.vetoerId())
                .map(id -> deputies.stream()
                        .filter(d -> d.id() == id)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Unknown vetoer: " + id)));
        return new SejmResult(r.number(), fromDto(r.bill()),
                new VoteResult(r.voteCounts(), r.passed(), vetoer));
    }
}
