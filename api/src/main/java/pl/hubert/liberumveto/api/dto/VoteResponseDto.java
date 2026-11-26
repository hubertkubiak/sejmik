package pl.hubert.liberumveto.api.dto;

/**
 * Response to a vote: what happened in this Sejm and the game state afterwards
 * (the next Sejm, or the end of the game).
 */
public record VoteResponseDto(SejmResultDto sejm, GameDto game) {
}
