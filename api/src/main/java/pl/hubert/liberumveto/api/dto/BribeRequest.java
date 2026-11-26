package pl.hubert.liberumveto.api.dto;

/**
 * Body of {@code POST /api/games/{id}/bribes}.
 */
public record BribeRequest(int deputyId, int amount) {
}
