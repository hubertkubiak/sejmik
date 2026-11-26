package pl.hubert.liberumveto.api.dto;

/**
 * Body of {@code PUT /api/games/{id}/selected-bill}.
 */
public record SelectBillRequest(int billId) {
}
