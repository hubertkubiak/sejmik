package pl.hubert.liberumveto.api.dto;

import pl.hubert.liberumveto.domain.game.SejmResult;
import pl.hubert.liberumveto.domain.model.Vote;
import pl.hubert.liberumveto.domain.sejm.VoteResult;

/**
 * The outcome of one Sejm: used both as the response to a vote and as a history entry.
 *
 * @param vetoer the deputy who dissolved the Sejm, or null
 */
public record SejmResultDto(
        int number,
        BillDto bill,
        long votesFor,
        long votesAgainst,
        long vetoes,
        boolean passed,
        boolean dissolved,
        DeputyDto vetoer) {

    public static SejmResultDto from(SejmResult sejmResult) {
        VoteResult result = sejmResult.result();
        return new SejmResultDto(
                sejmResult.number(),
                BillDto.from(sejmResult.bill()),
                result.count(Vote.FOR),
                result.count(Vote.AGAINST),
                result.count(Vote.VETO),
                result.passed(),
                result.dissolved(),
                result.vetoer().map(deputy -> DeputyDto.from(deputy, null)).orElse(null));
    }
}
